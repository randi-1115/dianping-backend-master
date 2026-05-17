package com.example.dianpingbackend.controller;

import com.example.dianpingbackend.entity.Result;
import com.example.dianpingbackend.service.LbsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lbs")
@CrossOrigin
public class LbsController {

    @Autowired
    private LbsService lbsService;

   /**
    * 查询附近店铺
    * @param longitude 用户经度
    * @param latitude  用户纬度
    * @param radius    搜索半径（米），默认3000
    */
   @GetMapping("/nearby")
    public Result nearbyShops(@RequestParam Double longitude,@RequestParam Double latitude,@RequestParam(defaultValue = "3000") Integer radius){
       return lbsService.nearbyShops(longitude,latitude,radius);
   }

    /**
     * 初始化店铺 GEO 数据（管理用）
     */
    @PostMapping("/init")
    public Result initGeo(){
        lbsService.initShopGeo();
        return Result.ok("店铺坐标已初始化到Redis");
    }

}
