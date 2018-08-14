package org.seckill.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.eto.Exposer;
import org.seckill.eto.SeckillExecution;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillClosedException;
import org.seckill.model.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
        "classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"})
public class SeckillServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(SeckillServiceTest.class);

    @Autowired
    private SeckillService seckillService;


    @Test
    public void getSeckillList() {
        List<Seckill> seckillList = seckillService.getSeckillList();
        logger.info("list={}", seckillList);
        /**
         * list=[
         * Seckill{seckillId=1000, Name='秒杀商品A', Number=100,startTime=Tue Jul 31 00:00:00 CST 2018,endTime=Wed Aug 01 00:00:00 CST 2018,createTime=Tue Jul 31 14:38:38 CST 2018},
         * Seckill{seckillId=1001, Name='秒杀商品B', Number=200, startTime=Tue Jul 31 00:00:00 CST 2018, endTime=Wed Aug 01 00:00:00 CST 2018, createTime=Tue Jul 31 14:38:38 CST 2018},
         * Seckill{seckillId=1002, Name='秒杀商品C', Number=300, startTime=Tue Jul 31 00:00:00 CST 2018, endTime=Wed Aug 01 00:00:00 CST 2018, createTime=Tue Jul 31 14:38:38 CST 2018},
         * Seckill{seckillId=1003, Name='秒杀商品D', Number=400, startTime=Tue Jul 31 00:00:00 CST 2018, endTime=Wed Aug 01 00:00:00 CST 2018, createTime=Tue Jul 31 14:38:38 CST 2018}
         * ]
         */

    }

    @Test
    public void getById() {
        long id = 1000L;
        Seckill seckill = seckillService.getById(id);
        logger.info("seckill={}", seckill);
        /**
         * seckill=Seckill{
         * seckillId=1000, Name='秒杀商品A', Number=100,
         * startTime=Tue Jul 31 00:00:00 CST 2018,
         * endTime=Wed Aug 01 00:00:00 CST 2018,
         * createTime=Tue Jul 31 14:38:38 CST 2018}
         */
    }

    @Test
    public void exportSeckillUrl() {
        long id = 1004L;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        if (exposer.isExposed()) {
            logger.info("exposer={}", exposer);
            long phone = 15682123575L;
            String md5 = "395206aa8d69a6a24f6a876c2a2ccd53";
            try {
                SeckillExecution seckillExecution = seckillService.excuteSeckill(id, phone, md5);
                logger.info("result={}", seckillExecution);
            } catch (RepeatKillException e1) {
                logger.error(e1.getMessage());
            } catch (SeckillClosedException e2) {
                logger.error(e2.getMessage());
            }
        } else {
            logger.warn("exposer={}", exposer);
        }
        /**
         * 10:48:11.676 [main] ERROR o.seckill.service.SeckillServiceTest - repeated kill
         * 更改电话号码再次执行
         * exposer=Exposer{exposed=true, md5='395206aa8d69a6a24f6a876c2a2ccd53', seckillId=1004, now=0, start=0, end=0}
         *
         * result=SeckillExecution{seckillId=1004, state=1, stateInfo='秒杀成功',
         * successKilled=SuccessKilled{seckillId=1004, userPhone=15682123575, state=-1, createTime=Thu Aug 09 10:48:57 CST 2018,
         * seckill=Seckill{seckillId=1004, Name='秒杀商品E', Number=498, startTime=Thu Aug 09 10:48:57 CST 2018, endTime=Sat Sep 01 00:00:00 CST 2018, createTime=Thu Aug 09 10:32:27 CST 2018}}}
         */
    }

    @Test
    public void executionSeckillProcedure() {
        long seckillId = 1003L;
        long phone = 28688262312L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()) {
            String md5 = exposer.getMd5();
            SeckillExecution seckillExecution = seckillService.excuteSeckillProcedure(seckillId, phone, md5);
            System.out.println(seckillExecution);
        }
    }
    /**
     * SeckillExecution{seckillId=1003, state=1, stateInfo='秒杀成功',
     * successKilled=SuccessKilled{seckillId=1003, userPhone=28688262312, state=-1, createTime=Mon Aug 13 16:40:12 CST 2018,
     * seckill=Seckill{seckillId=1003, Name='商品D', Number=394, startTime=Mon Aug 13 16:40:11 CST 2018, endTime=Fri Aug 31 00:00:00 CST 2018, createTime=Tue Jul 31 14:38:38 CST 2018}}}
     */

}