package org.seckill.dao;

import org.seckill.model.SuccessKilled;

public interface SuccessKilledDao {

    int insertSuccessKilled(long seckillId, long userPhone);

    SuccessKilled queryByIdWithSeckill(long seckillId);
}
