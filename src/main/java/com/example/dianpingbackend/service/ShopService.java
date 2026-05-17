package com.example.dianpingbackend.service;

import com.example.dianpingbackend.entity.Result;
import com.example.dianpingbackend.entity.Shop;
import java.util.List;

public interface ShopService {
    Result<List<Shop>> getAllShops();
    Result<Shop> getShopById(Long id);
    Result<String> updateShop(Shop shop);
    Result<String> addShop(Shop shop);
    Result<String> deleteShop(Long id);
    Result<List<Shop>> getShopsByCategory(String category);
    Result<List<Shop>> searchShops(String keyword);
}