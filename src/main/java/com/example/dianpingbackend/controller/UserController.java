package com.example.dianpingbackend.controller;

import com.example.dianpingbackend.entity.LoginRequest;
import com.example.dianpingbackend.entity.User;
import com.example.dianpingbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    // 获取验证码
    @GetMapping("/send-code")
    public String sendCode(@RequestParam String phone) {
        userService.getVerifyCode(phone);
        return "验证码已发送（模拟）";
    }

    // 登录
    @PostMapping("/login")
    public User login(@RequestBody LoginRequest loginRequest) {
        String phone = loginRequest.getPhone();
        String code = loginRequest.getCode();
        return userService.login(phone, code);
    }
}