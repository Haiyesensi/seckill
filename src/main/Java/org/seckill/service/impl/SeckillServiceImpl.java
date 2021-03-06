package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.eto.Exposer;
import org.seckill.eto.SeckillExecution;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillClosedException;
import org.seckill.exception.SeckillException;
import org.seckill.model.Seckill;
import org.seckill.model.SuccessKilled;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Component容器中的基本组件，不明类型
@Service
public class SeckillServiceImpl implements SeckillService {

    public static final String SALT = "asda806AG%$*^%$gasd78";

    private static Logger logger = LoggerFactory.getLogger(SeckillServiceImpl.class);

    @Autowired//@Resource,@Inject
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 10);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {

        //Redis缓存优化，超时的基础上维护一致性
        Seckill seckill = redisDao.getseckill(seckillId);
        if (seckill == null) {
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                return new Exposer(false, seckillId);
            } else {
                redisDao.putSeckill(seckill);
            }
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();

        if (startTime.getTime() > nowTime.getTime()
                || endTime.getTime() < nowTime.getTime()) {
            return new Exposer(false, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }

        String md5 = md5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    private String md5(long seckillId) {
        String base = seckillId + "/" + SALT;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }


    /**
     * 使用注解控制事务方法的优点：
     * 1.明确标注事务方法
     * 2.保证事务方法的执行时间尽可能短，将其他网络操作如HTTP请求，RPC等剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如单条的改动或者只读操作
     * <p>
     * <p>
     * 高并发优化：将insert操作提到update前面，减少行级锁的存在时间
     */
    @Override
    @Transactional
    public SeckillExecution excuteSeckill(long seckillId, long userPhone, String md5) throws SeckillException, SeckillClosedException, RepeatKillException {

        if (md5 == null || !md5.equals(md5(seckillId))) {
            throw new SeckillException("seckill data rewrite");
        }
        //执行秒杀逻辑：减库存 + 记录购买行为
        Date nowTime = new Date();
        int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
        try {
            if (insertCount <= 0) {
                throw new RepeatKillException("repeated kill");
            } else {
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0) {
                    //秒杀已经结束：时间或者库存原因
                    //用户只关心是否开始和结束
                    throw new SeckillClosedException("seckill is closed");
                } else {
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillClosedException e1) {
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //将所有编译器异常，转换为运行期异常
            throw new SeckillException("seckill inner error:" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public SeckillExecution excuteSeckillProcedure(long seckillId, long userPhone, String md5) throws SeckillException, SeckillClosedException, RepeatKillException {
        if (md5 == null || !md5.equals(md5(seckillId))) {
            return new SeckillExecution(seckillId, SeckillStateEnum.DATA_REWRITE);
        }
        //执行秒杀逻辑：减库存 + 记录购买行为
        Date killTime = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);

        try {
            seckillDao.killByProcedure(map);
            int result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, sk);
            } else {
                return new SeckillExecution(seckillId, SeckillStateEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
        }


    }
}

