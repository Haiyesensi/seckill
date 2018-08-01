package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.model.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;



// Junit启动时加载springIOC容器
@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit，spring配置文件的位置
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {

    @Resource
    private SeckillDao seckillDao;


    @Test
    public void queryById() {
        long id = 1000;
        Seckill seckill = seckillDao.queryById(id);
        System.out.println(seckill.getName());
        System.out.println(seckill);
    }

    @Test
    public void queryAll() {
        List<Seckill> seckillList = seckillDao.queryAll(0, 100);
        seckillList.stream().forEach(s -> System.out.println(s.toString()));
    }

    @Test
    public void reduceNumber() {
        int id = 1000;

        int count = seckillDao.reduceNumber(id, new Date());
        System.out.println(count);


    }
}