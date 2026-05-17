package com.example.dianpingbackend.mapper;

import com.example.dianpingbackend.entity.SeckillOrder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SeckillOrderMapper {
    @Insert("INSERT INTO seckill_order (voucher_id, user_id, state, create_time)" +  "VALUES (#{voucherId}, #{userId}, #{state}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")//返回自增主键
    int insert(SeckillOrder order);//插入秒杀订单
}