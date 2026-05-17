package com.example.dianpingbackend.entity;

import lombok.Data;
import java.time.LocalDateTime;
@Data
public class User {
    private Long id;
    private String phone;
    private String nickname;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
