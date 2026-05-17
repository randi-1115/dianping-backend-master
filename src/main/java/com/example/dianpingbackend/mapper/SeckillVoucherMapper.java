package com.example.dianpingbackend.mapper;

import com.example.dianpingbackend.entity.SeckillVoucher;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SeckillVoucherMapper {
    @Select("SELECT * FROM seckill_voucher WHERE voucher_id = #{voucherId}")
    SeckillVoucher selectById(Long voucherId);//根据id查询秒杀优惠券
    @Update("UPDATE seckill_voucher SET stock = stock - 1 WHERE voucher_id = #{voucherId} AND stock > 0")
    int deductStock(Long voucherId);

    @Update("UPDATE seckill_voucher SET stock = stock + 1 WHERE voucher_id = #{voucherId}")
    int incrementStock(Long voucherId);//扣减库存

}
