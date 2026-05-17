package com.example.dianpingbackend.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class RedisLock {
    private StringRedisTemplate redisTemplate;
    private String lockKey;
    private String lockValue;
    private long expireTime;

    private static final String UNLOCK_SCRIPT =
            "if redis.call('get',KEYS[1]) == ARGV[1] then " +
            "  return redis.call('del',KEYS[1]) " +
            "else " +
            "  return 0 " +
            "end";

    public RedisLock(StringRedisTemplate redisTemplate, String lockKey, String lockValue, long expireTime) {
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
        this.lockValue = lockValue;
        this.expireTime = expireTime;
    }

    public boolean tryLock() {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, expireTime, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    public void unlock() {
        redisTemplate.execute(
                new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class),
                Collections.singletonList(lockKey),
                lockValue
        );
    }
}
