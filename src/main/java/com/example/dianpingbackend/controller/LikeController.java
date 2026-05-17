package com.example.dianpingbackend.controller;

import com.example.dianpingbackend.entity.Result;
import com.example.dianpingbackend.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/like")
@CrossOrigin
public class LikeController {
    @Autowired
    private LikeService likeService;

    /**
     * 点赞/取消点赞
     * @param shopId 店铺ID
     * @param userId 从请求头获取当前用户ID
     */
    @PostMapping("/{shopId}")
    public Result likeOrUnlike(@PathVariable Long shopId,@RequestHeader("X-User-Id") Long userId){
        return likeService.likeOrUnlike(shopId, userId);
    }
    /**
     * 查询某店铺的点赞状态和点赞数
     */
    @GetMapping("/status/{shopId}")
    public Result getLikeStatus(@PathVariable Long shopId,@RequestHeader("X-User-Id") Long userId){
        return likeService.getLikeStatus(shopId,userId);
    }
}

