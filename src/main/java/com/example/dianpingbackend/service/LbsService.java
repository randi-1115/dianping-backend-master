package com.example.dianpingbackend.service;

import com.example.dianpingbackend.entity.Result;

public interface LbsService {
    Result nearbyShops(Double longitude,Double latitude , Integer radius);
    void initShopGeo();
}
