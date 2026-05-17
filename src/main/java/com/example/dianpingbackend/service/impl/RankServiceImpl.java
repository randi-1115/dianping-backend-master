package com.example.dianpingbackend.service.impl;

import com.example.dianpingbackend.entity.Result;
import com.example.dianpingbackend.service.RankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class RankServiceImpl implements RankService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String RANK_KEY = "rank:shop:like";

    @Override
    public Result getShopLikeRank(int top){//获取点赞排行榜
        //Set<ZSetOperations.TypedTuple<String>>：
        // ZSetOperations.TypedTuple是一个接口，表示有序集合中的一个元素及其分数。getValue()：元素本身,getScore()：对应的分数
        Set<ZSetOperations.TypedTuple<String>> tupleSet=
                redisTemplate.opsForZSet().reverseRangeWithScores(RANK_KEY,0,top-1);//获取排行榜前top个店铺，按照分数从高到低排序
        if(tupleSet == null || tupleSet.isEmpty()){
            return Result.ok("暂无排行榜数据", Collections.emptyList());
        }

        List<Map<String,Object>> rankList = new ArrayList<>();//排行榜列表，每个元素是一个Map，包含店铺ID和点赞数
        for(ZSetOperations.TypedTuple<String> tuple : tupleSet){
            Map<String,Object> item = new LinkedHashMap<>();//LinkedHashMap保持插入顺序
            item.put("shopId",Long.valueOf(tuple.getValue()));//店铺ID，tuple.getValue()返回的是字符串，需要转换为Long
            item.put("likeCount",tuple.getScore().longValue());//点赞数，tuple.getScore()返回的是Double，需要转换为Long
            rankList.add(item);//将店铺ID和点赞数的Map添加到排行榜列表中
        }
        return Result.ok("success",rankList);
    }

}
