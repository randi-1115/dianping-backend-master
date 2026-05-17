package com.example.dianpingbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeckillVoucher {
    private Long voucherId;//优惠券id
    private Long shopId;//商户id
    private String title;//优惠券标题
    private Integer stock;//库存
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
