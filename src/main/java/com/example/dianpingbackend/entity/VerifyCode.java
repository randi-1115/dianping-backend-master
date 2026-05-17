package com.example.dianpingbackend.entity;

import lombok.Data;
import java.time.LocalDateTime;
@Data
public class VerifyCode {
    private String phone;
    private String code;
    private LocalDateTime expireTime;
}
