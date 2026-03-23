package com.lzz.backend.service;

import com.lzz.backend.dto.AgentRecommendRequest;
import com.lzz.backend.dto.AgentRecommendResponse;

public interface AgentService {
    AgentRecommendResponse recommend(AgentRecommendRequest request);
}
