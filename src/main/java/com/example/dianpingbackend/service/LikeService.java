package com.example.dianpingbackend.service;

import com.example.dianpingbackend.entity.Result;

public interface LikeService {
    Result likeOrUnlike(Long shopId,Long userId);
    Result getLikeStatus(Long shopId,Long userId);
}
