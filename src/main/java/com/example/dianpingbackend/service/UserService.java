package com.example.dianpingbackend.service;

import com.example.dianpingbackend.entity.User;
public interface UserService {

    String getVerifyCode(String phone);

    User login(String phone, String code);
}
