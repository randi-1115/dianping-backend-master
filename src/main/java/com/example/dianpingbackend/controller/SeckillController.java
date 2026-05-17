package com.example.dianpingbackend.controller;

import com.example.dianpingbackend.entity.Result;
import com.example.dianpingbackend.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seckill")
@CrossOrigin
public class SeckillController {
    @Autowired
    private SeckillService seckillService;

    @PostMapping("/{voucherId}")
    public Result<String> seckill(
            @PathVariable Long voucherId,//从路径变量获取秒杀券Id
            @RequestHeader("X-User-Id") Long userId  //从请求头获取用户Id，前端需要在请求头中添加X-User-Id字段，值为当前登录用户的Id
    ) {
        return seckillService.seckill(voucherId, userId);
    }
    @PostMapping("/init/{voucherId}")
    public Result<String> initStock(@PathVariable Long voucherId) {
        seckillService.initStock(voucherId);
        return Result.ok("Redis库存初始化完成");
    }
}
