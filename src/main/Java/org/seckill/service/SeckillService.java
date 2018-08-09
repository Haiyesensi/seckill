package org.seckill.service;

import org.seckill.eto.Exposer;
import org.seckill.eto.SeckillExecution;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillClosedException;
import org.seckill.exception.SeckillException;
import org.seckill.model.Seckill;

import java.util.List;

/**
 * 业务接口的书写：站在使用者的角度来编写设计接口
 * 1.方法定义的粒度； 2.明确的参数列表； 3.明确的返回类型或异常
 */
public interface SeckillService {


    /**
     * 查询所有秒杀记录
     *
     * @return
     */
    List<Seckill> getSeckillList();


    /**
     * 查询单个秒杀记录
     *
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);


    /**
     * 秒杀开启时输出秒杀接口地址，否则输出系统时间和秒杀时间
     *
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀，秒杀验证
     */
    SeckillExecution excuteSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, SeckillClosedException, RepeatKillException;
}
