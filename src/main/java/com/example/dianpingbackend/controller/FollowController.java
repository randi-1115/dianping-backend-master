package com.example.dianpingbackend.controller;

import com.example.dianpingbackend.entity.Result;
import com.example.dianpingbackend.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow")
@CrossOrigin
public class FollowController {
    @Autowired
    private FollowService followService;

    /**
     * 关注/取消关注店铺
     * @param shopId 店铺ID
     * @param userId 当前用户ID（从请求头获取）
     */
    @PostMapping("/{shopId}")
    public Result followOrUnfollow(@PathVariable Long shopId,@RequestHeader("X-User-Id") Long userId){
        return followService.followOrUnfollow(shopId,userId);
    }

    /**
     * 查询当前用户对某店铺的关注状态
     * 同时返回该用户的关注总数和该店铺的粉丝数
     */
    @GetMapping("/status/{shopId}")
    public Result getFollowStatus(@PathVariable Long shopId,@RequestHeader("X-User-Id") Long userId){
        return followService.getFollowStatus(shopId,userId);
    }

}
