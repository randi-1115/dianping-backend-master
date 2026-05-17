package com.example.dianpingbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeckillOrder {
    private Long id;//秒杀订单id
    private Long voucherId;//秒杀活动id
    private Long userId;//用户id
    private Integer state;//0-待支付
    private LocalDateTime createTime;
}
