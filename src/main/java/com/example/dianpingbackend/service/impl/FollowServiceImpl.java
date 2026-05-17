package com.example.dianpingbackend.service.impl;

import com.example.dianpingbackend.entity.Result;
import com.example.dianpingbackend.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FollowServiceImpl implements FollowService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String USER_FOLLOW_KEY = "user:follow:";
    private static final String SHOP_FANS_KEY = "shop:fans:";

    @Override
    public Result followOrUnfollow(Long shopId,Long userId){

        String userFollowKey = USER_FOLLOW_KEY + userId;
        String shopfansKey = SHOP_FANS_KEY + shopId;

        Boolean isFollowing = redisTemplate.opsForSet().isMember(userFollowKey,shopId.toString());//判断用户是否已关注店铺

        if (Boolean.TRUE.equals(isFollowing)){
            redisTemplate.opsForSet().remove(userFollowKey,shopId.toString());
            redisTemplate.opsForSet().remove(shopfansKey,userId.toString());
            return Result.ok("已取消关注");
        }else{
            redisTemplate.opsForSet().add(userFollowKey,shopId.toString());
            redisTemplate.opsForSet().add(shopfansKey,userId.toString());
            return Result.ok("关注成功");
        }

    }
    @Override
    public Result getFollowStatus(Long shopId,Long userId){//查询关注状态和粉丝数量
        String userFollowKey = USER_FOLLOW_KEY + userId;
        String shopFansKey = SHOP_FANS_KEY +  shopId;
        Boolean isFollowed = redisTemplate.opsForSet().isMember(userFollowKey,shopId.toString());//判断用户是否已关注店铺
        Long userFollowCount = redisTemplate.opsForSet().size(userFollowKey);//获取用户关注的店铺数量 集合大小
        Long shopFansCount = redisTemplate.opsForSet().size(shopFansKey);//获取店铺粉丝数量 集合大小

        Map<String,Object> data = new HashMap<>();
        data.put("followed",isFollowed);//是否已关注
        data.put("userFollowCount",userFollowCount);
        data.put("shopFansCount",shopFansCount);
        return Result.ok("查询成功",data);
    }
}

