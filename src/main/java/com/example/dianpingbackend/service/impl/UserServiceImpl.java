// src/main/java/com/example/dianpingbackend/service/impl/UserServiceImpl.java
package com.example.dianpingbackend.service.impl;

import com.example.dianpingbackend.entity.User;
import com.example.dianpingbackend.mapper.UserMapper;
import com.example.dianpingbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    // Redis 键前缀（避免键冲突）
    private static final String VERIFY_CODE_KEY_PREFIX = "dianping:verify:code:";
    // 验证码有效期（5分钟）
    private static final long CODE_EXPIRE_MINUTES = 5;

    @Autowired
    private UserMapper userMapper;

    // 注入 Redis 模板（操作字符串类型）
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String getVerifyCode(String phone) {
        // 1. 生成6位数字验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 2. 存储到 Redis（键：前缀+手机号，值：验证码，过期时间：5分钟）
        String redisKey = VERIFY_CODE_KEY_PREFIX + phone;
        stringRedisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 3. 模拟发送短信（控制台打印）
        System.out.println("【大众点评】您的验证码是：" + code + "，5分钟内有效，请勿泄露给他人");
        return code;
    }

    @Override
    public User login(String phone, String code) {
        // 1. 从 Redis 获取验证码
        String redisKey = VERIFY_CODE_KEY_PREFIX + phone;
        String storedCode = stringRedisTemplate.opsForValue().get(redisKey);

        // 2. 验证验证码（为空/不匹配/过期）
        if (storedCode == null || !storedCode.equals(code)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 3. 查询用户（新用户自动注册）
        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setNickname("用户" + phone.substring(7)); // 默认昵称（手机号后4位）
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.insert(user);
        }

        // 4. 登录成功，删除 Redis 中的验证码（避免重复使用）
        stringRedisTemplate.delete(redisKey);

        return user;
    }
}