package com.example.dianpingbackend.service.impl;

import com.example.dianpingbackend.entity.Result;
import com.example.dianpingbackend.entity.SeckillOrder;
import com.example.dianpingbackend.entity.SeckillVoucher;
import com.example.dianpingbackend.entity.User;
import com.example.dianpingbackend.mapper.SeckillOrderMapper;
import com.example.dianpingbackend.mapper.SeckillVoucherMapper;
import com.example.dianpingbackend.mapper.UserMapper;
import com.example.dianpingbackend.service.SeckillService;
import com.example.dianpingbackend.utils.RedisLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SeckillVoucherMapper voucherMapper;

    @Autowired
    private SeckillOrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            10, 20, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
        new ThreadPoolExecutor.CallerRunsPolicy()
    );

    //库存前缀和用户订单前缀
    private static final String STOCK_KEY="seckill:stock:";

    private static final String USER_ORDER_KEY = "seckill:user:";

    @Override
    public Result<String> seckill(Long voucherId, Long userId){
        //1. 校验用户是否存在
        User user = userMapper.selectById(userId);
        if(user == null){
            return Result.fail(404,"用户不存在！");
        }
        //校验秒杀时间
        SeckillVoucher voucher = voucherMapper.selectById(voucherId);
        if(voucher == null){
            return Result.fail(404, "秒杀活动不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if(now.isBefore(voucher.getStartTime())){
            return Result.fail(400, "活动尚未开始");
        }
        if(now.isAfter(voucher.getEndTime())){
            return Result.fail(400, "活动已结束");
        }

        //2.加分布式锁（防重复下单）
        String lockKey = "lock:seckill:" + userId + ":" + voucherId;//锁的key 包含用户id和优惠卷id，确保每个用户对每个优惠卷只能有一个锁，防止重复下单
        String lockValue = UUID.randomUUID().toString();//锁的value 用UUID防止误删 锁的签名 UUID是一串全球唯一的随机字符串
        RedisLock lock = new RedisLock(redisTemplate, lockKey, lockValue, 10);
        try {
            if(!lock.tryLock()){
                return Result.fail(429, "请勿重复提交");
            }
            //3.判断是否已经购买
            String userOrderKey = USER_ORDER_KEY + voucherId;
            //判断用户是否已经购买过该优惠卷，使用Redis的Set数据结构来存储已经购买的用户ID，通过isMember方法检查用户ID是否在集合中，如果在集合中，说明用户已经购买过该优惠卷，返回相应的提示信息。
            Boolean hasOrdered = redisTemplate.opsForSet().isMember(userOrderKey, userId.toString());
            if(Boolean.TRUE.equals(hasOrdered)){
                return Result.fail(400, "您已抢过该优惠券");
            }
            //4.预扣库存
            String stockKey = STOCK_KEY + voucherId;
            Long stock = redisTemplate.opsForValue().decrement(stockKey);//预扣库存，使用Redis的decrement方法将库存数量减1，如果减完后库存数量小于0，说明库存不足，需要将库存数量加回去，并返回相应的提示信息。
            if(stock == null || stock<0){
                redisTemplate.opsForValue().increment(stockKey);
                return Result.fail(400, "优惠券已抢光");
            }
            //5.标记用户已经购买
            //为了防止用户重复购买，我们需要在Redis中记录已经购买过该优惠卷的用户ID。
            // 我们可以使用Redis的Set数据结构来存储已经购买的用户ID，通过add方法将用户ID添加到集合中，这样在下次用户尝试购买时，我们就可以通过isMember方法检查用户ID是否在集合中，如果在集合中，说明用户已经购买过该优惠卷，返回相应的提示信息。
            redisTemplate.opsForSet().add(userOrderKey,userId.toString());
            //6.异步处理订单
            //为了提高系统的响应速度，我们可以将订单的处理放在一个独立的线程中，这样主线程就可以快速响应用户的请求，提高系统的吞吐量和响应速度
            executor.submit(() -> {
                boolean deducted = false;
                try {
                    int rows = voucherMapper.deductStock(voucherId);
                    if (rows == 0) {
                        redisTemplate.opsForValue().increment(stockKey);
                        redisTemplate.opsForSet().remove(userOrderKey, userId.toString());
                        return;
                    }
                    deducted = true;
                    SeckillOrder order = new SeckillOrder();
                    order.setVoucherId(voucherId);
                    order.setUserId(userId);
                    order.setState(0);
                    orderMapper.insert(order);
                } catch (Exception e) {
                    log.error("秒杀异步下单失败 userId={} voucherId={}", userId, voucherId, e);
                    redisTemplate.opsForValue().increment(stockKey);
                    redisTemplate.opsForSet().remove(userOrderKey, userId.toString());
                    if (deducted) {
                        voucherMapper.incrementStock(voucherId);
                    }
                }
            });
            return Result.ok("抢购成功，订单处理中");
        }finally {
            lock.unlock();
        }


    }
    //系统初始化时加载库存到Redis
    public void initStock(Long voucherId){
        SeckillVoucher voucher = voucherMapper.selectById(voucherId);//从数据库中查询优惠卷信息，根据优惠卷ID获取对应的库存数量，
        // 并将其加载到Redis中，以便在秒杀过程中进行库存的预扣和扣减操作。
        if(voucher != null){
            String stockKey = STOCK_KEY + voucherId;
            redisTemplate.opsForValue().set(stockKey,String.valueOf(voucher.getStock()));
            String userKey = USER_ORDER_KEY + voucherId;
            redisTemplate.delete(userKey);
        }
    }


}
