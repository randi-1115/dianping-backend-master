package com.example.dianpingbackend.utils;

public class CacheConstants {
    public static final String CACHE_SHOP_KEY = "cache:shop:";//缓存店铺信息的key前缀
    public static final String LOCK_SHOP_KEY = "lock:shop:";//分布式锁的key前缀
    public static final Long CACHE_NULL_TTL = 60L;//缓存空值的TTL，单位为秒，防止缓存穿透
    public static final Integer CACHE_SHOP_TTL = 30;//缓存店铺信息的TTL，单位为分钟，防止缓存雪崩
    public static final Long LOCK_TTL = 10L;//分布式锁的TTL，单位为秒，防止死锁

    public static final Integer CACHE_SHOP_ALL_TTL = 5;//列表缓存5分钟
    public static final Integer CACHE_CATEGORY_TTL = 10;//分类缓存10分钟
    public static final Integer CACHE_SEARCH_TTL = 2;//搜索缓存2分钟
}
