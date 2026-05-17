// LbsServiceImpl.java
package com.example.dianpingbackend.service.impl;

import com.example.dianpingbackend.entity.Result;
import com.example.dianpingbackend.entity.Shop;
import com.example.dianpingbackend.mapper.ShopMapper;
import com.example.dianpingbackend.service.LbsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LbsServiceImpl implements LbsService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ShopMapper shopMapper;

    private static final String GEO_KEY = "shop:geo";

    // 初始化：将所有店铺经纬度添加到 Redis GEO
    @Override
    public void initShopGeo() {
        List<Shop> shops = shopMapper.selectAll();
        // 构造 GEO 位置集合
        List<RedisGeoCommands.GeoLocation<String>> locations = shops.stream()//把店铺列表变成一个“流水线”一个个处理·
                .filter(shop -> shop.getLongitude() != null && shop.getLatitude() != null
                && (shop.getLongitude().compareTo(BigDecimal.ZERO)!= 0 || shop.getLatitude().compareTo(java.math.BigDecimal.ZERO) != 0))//过滤掉经纬度为空的店铺
                .map(shop -> new RedisGeoCommands.GeoLocation<>(
                        shop.getId().toString(),//店铺 ID 作为 GEO 的 member
                        new Point(shop.getLongitude().doubleValue(), shop.getLatitude().doubleValue())//把经纬度转换成 Point 对象
                ))
                .collect(Collectors.toList());//把处理后的结果收集成一个列表
        if (!locations.isEmpty()) {
            // 先删除旧数据，再批量添加
            redisTemplate.delete(GEO_KEY);
            redisTemplate.opsForGeo().add(GEO_KEY, locations);
            log.info("成功初始化 {} 个店铺坐标", locations.size());
        }
    }

    // 查询附近店铺
    @Override
    public Result nearbyShops(Double longitude, Double latitude, Integer radius) {
        // 1. 调用 Redis GEO，返回半径内的店铺及距离
        Distance distance =new Distance(radius, RedisGeoCommands.DistanceUnit.METERS);//搜索半径
        Circle circle = new Circle(new Point(longitude, latitude), distance);//以用户位置为圆心，搜索半径为半径，构造一个圆形区域
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .sortAscending();  // 由近到远排序

        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults =
                redisTemplate.opsForGeo().radius(GEO_KEY, circle, args);//GeoResults：一个包含搜索结果的容器。
        // GeoLocation：表示 GEO 中的一个位置，包含 member（店铺 ID）和 Point（经纬度）。
        // radius：搜索半径，单位由 Distance 对象指定。
        //每个 GeoResult 包含：getContent()：GeoLocation，包含成员的 name（店铺 ID），getDistance()：距离圆心多少米
        if (geoResults == null || geoResults.getContent().isEmpty()) {
            return Result.ok("附近暂无店铺", Collections.emptyList());
        }

        // 2. 提取店铺ID和距离，保持顺序
        List<Long> shopIds = new ArrayList<>();
        Map<Long, Double> distanceMap = new LinkedHashMap<>(); // 保持顺序
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> geoResult : geoResults) {
            String member = geoResult.getContent().getName();// 店铺ID字符串
            Long shopId = Long.valueOf(member);// 转换为 Long 类型
            double dist = geoResult.getDistance().getValue(); // 距离：米
            shopIds.add(shopId);// 添加到店铺ID列表
            distanceMap.put(shopId, dist);// 添加到距离映射
        }

        // 3. 批量查询店铺详情（利用已有的 ShopMapper）
        List<Shop> shops = shopMapper.selectBatchIds(shopIds); // 需要在 Mapper 中增加该方法
        // 转化为 Map 方便查找
        Map<Long, Shop> shopMap = shops.stream()
                .collect(Collectors.toMap(Shop::getId, s -> s));

        // 4. 组装返回结果，保持距离排序
        List<Map<String, Object>> resultList = new ArrayList<>();//最终返回的店铺列表，每个店铺是一个 Map，包含店铺信息和距离
        for (Long shopId : shopIds) {
            Shop shop = shopMap.get(shopId);//根据店铺ID获取店铺信息
            if (shop != null) {
                Map<String, Object> map = new LinkedHashMap<>();//使用 LinkedHashMap 保持字段顺序
                map.put("shopId", shop.getId());
                map.put("name", shop.getName());
                map.put("address", shop.getAddress());
                map.put("score", shop.getScore());
                map.put("distance", Math.round(distanceMap.get(shopId))); // 四舍五入米
                // 还可以加入其他字段
                resultList.add(map);
            }
        }

        return Result.ok("查询成功", resultList);
    }
}