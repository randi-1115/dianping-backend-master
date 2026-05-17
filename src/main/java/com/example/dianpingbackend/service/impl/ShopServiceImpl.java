package com.example.dianpingbackend.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.dianpingbackend.entity.Result;
import com.example.dianpingbackend.entity.Shop;
import com.example.dianpingbackend.mapper.ShopMapper;
import com.example.dianpingbackend.service.ShopService;
import com.example.dianpingbackend.utils.CacheConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopMapper shopMapper;
    @Autowired
    private Cache<String , Object> localCache;//本地缓存对象
    @Autowired
    private StringRedisTemplate redisTemplate;//Redis操作对象

    @Override
    //只加Redis缓存并设置较短TTL
    public Result<List<Shop>> getAllShops() {
        //查询Redis缓存
        String key = "cache:shop:all";
        String json = redisTemplate.opsForValue().get(key);
        //如果缓存中有数据，直接返回
        if(StrUtil.isNotBlank(json)){
            return Result.ok("查询成功", JSONUtil.toList(json, Shop.class));
        }
        //如果缓存中没有数据，从数据库中查询，并将结果存入缓存，设置较短的TTL（过期时间）
        List<Shop> shops = shopMapper.selectAll();
        redisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shops),5, TimeUnit.MINUTES);
        return Result.ok("查询成功", shops);
    }

    @Override
    //根据id查询店铺信息，先从本地缓存中查询，如果没有再从redis中查询，如果还没有再从数据库中查询，并将结果存入缓存
    public Result<Shop> getShopById(Long id){
        String key= CacheConstants.CACHE_SHOP_KEY + id;
        //先从本地缓存中查询
        Shop shop = (Shop) localCache.getIfPresent(key);//从本地缓存中获取数据并转化为Shop对象
        if (shop != null){
            System.out.println("✅ 命中 Caffeine 本地缓存，id=" + id);
            return Result.ok("查询成功", shop);
        }
        //查询redis缓存
        String shopJson = redisTemplate.opsForValue().get(key);//去Redis中获取数据，根据key取value，返回字符串
        //判段是否命中空值标记（防穿透）
        // 穿透：攻击者不断请求不存在的数据，导致数据库压力过大，甚至崩溃。
        // 解决方法：当查询结果为空时，在缓存中存储一个空值，并设置较短的TTL（过期时间）。这样后续请求同样的数据时，就会直接从缓存中获取到空值，而不会访问数据库，从而保护数据库免受攻击。
        if("".equals(shopJson)){
            System.out.println("⚠️ 命中空值标记，id=" + id);
            return Result.fail(404, "店铺不存在");
        }//如果shopJson是空字符串，说明之前查询过这个id但没有找到对应的Shop对象，我们在缓存中存储了一个空字符串作为标记，表示这个id不存在对应的Shop对象。这样后续请求同样的id时，就会直接从缓存中获取到空字符串，而不会访问数据库，从而防止缓存穿透。
        if(StrUtil.isNotBlank(shopJson)){ //HUtool工具库 是否为空白（null、空字符串、空格等都算空白）
            System.out.println("✅ 命中 Redis 缓存，id=" + id);
            shop = JSONUtil.toBean(shopJson, Shop.class);//将JSON字符串转换为Shop对象
            //写入本地缓存
            localCache.put(key, shop);//将从Redis获取的数据放入本地缓存中，key是缓存的key，shop是对应的值
            return Result.ok("查询成功", shop);
        }
        //使用互斥锁防止缓存击穿
        //缓存击穿：当某个热点数据（访问量很高的数据）在缓存中失效时，可能会有大量的请求同时访问数据库，导致数据库压力过大，甚至崩溃。
        //互斥锁：当多个线程同时请求同一个数据时，只有一个线程能够获得锁并查询数据库，其他线程需要等待锁释放后才能继续执行。这样可以避免多个线程同时查询数据库导致的缓存击穿问题。
        String lockKey = CacheConstants.LOCK_SHOP_KEY + id;
        try {
            //尝试获得分布式锁
            //setIfAbsent方法：如果key不存在，则设置key的值为value，并返回true；如果key已经存在，则不做任何操作，并返回false。
            // 通过这个方法，我们可以确保只有一个线程能够成功获得锁，其他线程需要等待锁释放后才能继续执行。
            Boolean locked = redisTemplate.opsForValue()//opsForValue()方法：表示要操作字符串类型数据
                    .setIfAbsent(lockKey,"1", CacheConstants.LOCK_TTL, TimeUnit.SECONDS);
            //(锁名，锁值，过期时间，时间单位）
            if (Boolean.TRUE.equals(locked)){
                //获取锁成功，再次查询redis缓存，防止在获取锁的过程中其他线程已经将数据写入缓存
                shopJson = redisTemplate.opsForValue().get(key);//再次从Redis中获取数据，根据key取value，返回字符串
                if (StrUtil.isNotBlank(shopJson)){
                    //如果shopJson不为空，说明在获取锁的过程中其他线程已经将数据写入缓存，我们可以直接返回缓存中的数据
                    shop = JSONUtil.toBean(shopJson,Shop.class);//将JSON字符串转换为Shop对象
                    localCache.put(key,shop);//将从Redis获取的数据放入本地缓存中，key是缓存的key，shop是对应的值
                    return Result.ok("查询成功", shop);
                }
                //为空防击穿
                if ("".equals(shopJson)){
                    return Result.fail(404, "店铺不存在");
                }
                //查数据库
                System.out.println("❌ 缓存未命中，查询数据库，id=" + id);
                shop = shopMapper.selectById(id);
                //加随机时间防止雪崩
                if(shop ==null){
                    //缓存空对象防止穿透
                    int randomOffset = new Random().nextInt(10);//生成0到9之间的随机数
                    redisTemplate.opsForValue().set(key,"", CacheConstants.CACHE_NULL_TTL+randomOffset, TimeUnit.SECONDS);
                    return Result.fail(404, "店铺不存在");
                }
                //写入redis缓存和本地缓存
                //将shop对象转换为JSON字符串，并存入Redis缓存中，设置过期时间为CacheConstants.CACHE_SHOP_TTL分钟
                int baseTtl = 30;
                int randomOffset = new Random().nextInt(5);
                redisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),
                        baseTtl+randomOffset, TimeUnit.MINUTES);
                localCache.put(key,shop);
                return Result.ok("查询成功", shop);
            }else{//获取锁失败，说明有其他线程正在查询数据库，我们等待一段时间后重试
                Thread.sleep(50);//等待50毫秒后重试
                return getShopById(id);//递归调用getShopById方法，继续尝试获取数据 (现在也返回Result了)
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            // 降级：直接查数据库
            shop = shopMapper.selectById(id);
            return shop != null ? Result.ok("查询成功", shop) : Result.fail(404, "店铺不存在");
        }finally {
            //释放锁 无论怎样 还锁
            redisTemplate.delete(lockKey);
        }
    }
    @Override
    @Transactional//事务
    public Result<String> updateShop(Shop shop) {
        //先更新数据库
        int rows = shopMapper.update(shop);
        //
        if(rows > 0){
            //删除Redis缓存
            String key = CacheConstants.CACHE_SHOP_KEY + shop.getId();
            redisTemplate.delete(key);
            //本地缓存有TTL自动过期 无需处理
            return Result.ok("更新成功");
        }
        return Result.fail(400, "更新失败，店铺ID可能不存在");
    }
    //增删操作 同步清理缓存
    @Override
    public Result<String> addShop(Shop shop) {
        int rows = shopMapper.insert(shop);
        if (rows > 0) {
            //新增商铺后，清除相关的缓存，以确保数据的一致性
            redisTemplate.delete("cache:shop:all");//删除所有商铺列表的缓存，确保下次查询时能够获取到最新的数据
            return Result.ok("新增成功");
        }
        return Result.fail(400, "新增失败");
    }

    @Override
    @Transactional
    public Result<String> deleteShop(Long id) {
        int rows = shopMapper.deleteById(id);
        if(rows>0){
            String key = CacheConstants.CACHE_SHOP_KEY + id;
            redisTemplate.delete(key);
            redisTemplate.delete("cache:shop:all");
            return Result.ok("删除成功");
        }
        return Result.fail(400, "删除失败，店铺不存在");
    }

    @Override
    //根据分类查询商铺列表
    //为了提高查询效率，我们可以在Redis中使用一个Hash数据结构来存储每个分类对应的商铺列表。
    // Hash是一种键值对集合，可以通过分类作为key，商铺列表作为value进行存储和查询。
    public Result<List<Shop>> getShopsByCategory(String category) {
        String key = "cache:shop:category:"+category;
        String json = redisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(json)){
            return Result.ok("查询成功", JSONUtil.toList(json, Shop.class));
        }
        List<Shop> shops = shopMapper.selectByCategory(category);
        redisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shops),10, TimeUnit.MINUTES);
        return Result.ok("查询成功", shops);
    }
    @Override
    public Result<List<Shop>> searchShops(String keyword) {
        String key = "cache:shop:search:"+keyword;
        String json = redisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(json)){
            return Result.ok("查询成功", JSONUtil.toList(json, Shop.class));
        }
        List<Shop> shops = shopMapper.selectByKeyword(keyword);
        redisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shops),2, TimeUnit.MINUTES);
        return Result.ok("查询成功", shops);
    }
}