package com.example.dianpingbackend.service;

import com.example.dianpingbackend.entity.Result;

public interface FollowService {
    Result followOrUnfollow(Long targetUserId,Long userId);
    Result getFollowStatus(Long shopId,Long userId);
}
