package com.example.dianpingbackend.service.impl;

import com.example.dianpingbackend.entity.Result;
import com.example.dianpingbackend.mapper.ShopMapper;
import com.example.dianpingbackend.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeServiceImpl implements LikeService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ShopMapper shopMapper;

    private static final String LIKE_KEY = "like:shop:";
    private static final String RANK_KEY = "rank:shop:like";

    @Override
    public Result likeOrUnlike(Long shopId,Long userId){
        if(shopMapper.selectById(shopId)== null){
            return Result.fail(404,"店铺不存在");
        }
        String key = LIKE_KEY + shopId;
        Boolean isMember = redisTemplate.opsForSet().isMember(key,userId.toString());//判断用户是否点赞

        if (Boolean.TRUE.equals(isMember)){
            redisTemplate.opsForSet().remove(key,userId.toString());
            redisTemplate.opsForZSet().incrementScore(RANK_KEY,shopId.toString(),-1);
            return Result.ok("已取消点赞");
        }else {
            redisTemplate.opsForSet().add(key,userId.toString());
            redisTemplate.opsForZSet().incrementScore(RANK_KEY,shopId.toString(),1);
            return Result.ok("已点赞");
        }
    }
    @Override
    public Result getLikeStatus(Long shopId,Long userId){
        String key = LIKE_KEY + shopId;
        Boolean isMember = redisTemplate.opsForSet().isMember(key,userId.toString());//判断用户是否点赞
        Long count = redisTemplate.opsForSet().size(key);//获取点赞数量 集合大小
        //把两个结果打包进一个 Map，准备返回给前端
        java.util.Map<String ,Object> data = new java.util.HashMap<>();
        data.put("liked",isMember);//是否点赞
        data.put("likeCount",count);//点赞数量
        return Result.ok("查询成功",data);
    }
}
