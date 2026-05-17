package com.example.dianpingbackend.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration//这个类是配置说明 扫描该类
public class CaffeineConfig {

    @Bean//执行@Bean方法 然后放到Spring容器中，供程序使用（localCache())
    public Cache<String, Object> localCache() {//带自动过期功能的Map键值对集合
        //具体配置
        return Caffeine.newBuilder()//缓存构建器
                .initialCapacity(100)//设置初始容量为100
                .maximumSize(1000)//最大容量1000 超过会自动踢掉不常用的旧数据 防止内存溢出
                .expireAfterWrite(30, TimeUnit.SECONDS)  // 每条数据写入后30s过期 短TTL保证一致性
                .build();//构造缓存对象
    }
}