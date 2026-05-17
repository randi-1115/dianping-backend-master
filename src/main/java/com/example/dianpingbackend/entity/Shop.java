package com.example.dianpingbackend.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Shop {
    private Long id;
    private String name;
    private String address;
    private BigDecimal score;
    private String category;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
