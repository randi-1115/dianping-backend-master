package com.example.dianpingbackend.service;

import com.example.dianpingbackend.entity.Result;

public interface SeckillService {
    /**
     * 抢购秒杀优惠券
     * @param voucherId 优惠券ID
     * @param userId    用户ID
     * @return 抢购结果描述
     */
    Result<String> seckill(Long voucherId, Long userId);
    void initStock(Long voucherId);
}

