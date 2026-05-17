package com.example.dianpingbackend.controller;

import com.example.dianpingbackend.entity.Result;
import com.example.dianpingbackend.service.RankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rank")
@CrossOrigin
public class RankController {
    @Autowired
    private RankService rankService;

    /**
     * 获取点赞排行榜 (按点赞数降序)
     * @param top 取前N名，默认10
     */
    @GetMapping("/shop/like")
    public Result getLikeRank(@RequestParam (defaultValue = "10") int top){
        return rankService.getShopLikeRank(top);
    }
}
