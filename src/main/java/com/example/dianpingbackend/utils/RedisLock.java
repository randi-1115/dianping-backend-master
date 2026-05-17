package com.example.dianpingbackend.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;

public class RedisLock {
    private StringRedisTemplate redisTemplate;//Redis操作模板
    private String lockKey;//锁的key 名字
    private String lockValue;//锁的value 用UUID防止误删 锁的签名 UUID是一串全球唯一的随机字符串
    private long expireTime;//锁的过期时间，单位秒 防止死锁
    public RedisLock(StringRedisTemplate redisTemplate,String lockKey, String LockValue,long expireTime){
        this.redisTemplate = redisTemplate;
        this.lockKey = lockKey;
        this.lockValue = LockValue;
        this.expireTime = expireTime;
    }
    //尝试获取锁
    public boolean tryLock(){
        //setIfAbsent()方法是RedisTemplate提供的一个原子操作，用于在Redis中设置一个键值对，如果该键不存在则设置成功并返回true，如果该键已经存在则不进行任何操作并返回false。
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey,lockValue,expireTime, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
        //Boolean.TRUE.equals(success)的作用是将success转换为boolean类型，并且处理了可能出现的null值情况。如果success为null，则Boolean.TRUE.equals(success)会返回false；如果success为true，则返回true；如果success为false，则返回false。这样可以避免在使用Boolean对象时出现NullPointerException异常。
    }
//释放锁
    public void unlock() {
        String currentValue = redisTemplate.opsForValue().get(lockKey);//获取当前锁的值
        //只有当锁的值与当前线程持有的锁值相同的时候才删除锁，防止误删其他线程的锁
        if(lockValue.equals(currentValue)){
            redisTemplate.delete(lockKey);
        }
    }
}
