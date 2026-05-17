package com.example.dianpingbackend.controller;

import com.example.dianpingbackend.entity.Result;
import com.example.dianpingbackend.entity.Shop;
import com.example.dianpingbackend.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//内置web服务器，提供RESTful API接口，处理HTTP请求和响应
@RestController
@RequestMapping("/api/shops")
@CrossOrigin
public class ShopController {

    @Autowired
    private ShopService shopService;

    // 查询所有店铺
    @GetMapping
    public Result<List<Shop>> getAllShops() {
        return shopService.getAllShops();
    }

    // 按ID查询单个店铺
    @GetMapping("/{id}")
    public Result<Shop> getShopById(@PathVariable Long id) {
        return shopService.getShopById(id);
    }

    // 新增店铺
    @PostMapping
    public Result<String> addShop(@RequestBody Shop shop) {
        return shopService.addShop(shop);
    }

    // 更新店铺
    @PutMapping("/{id}")
    public Result<String> updateShop(@PathVariable Long id, @RequestBody Shop shop) {
        shop.setId(id); // 确保ID一致
        return shopService.updateShop(shop);
    }

    // 按ID删除店铺
    @DeleteMapping("/{id}")
    public Result<String> deleteShop(@PathVariable Long id) {
        return shopService.deleteShop(id);
    }

    // 按品类查询
    @GetMapping("/category")
    public Result<List<Shop>> getByCategory(@RequestParam String category) {
        return shopService.getShopsByCategory(category);
    }

    // 模糊搜索查询
    @GetMapping("/search")
    public Result<List<Shop>> search(@RequestParam String keyword) {
        return shopService.searchShops(keyword);
    }
}