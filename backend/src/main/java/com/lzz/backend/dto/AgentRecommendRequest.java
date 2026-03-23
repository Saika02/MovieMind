package com.lzz.backend.dto;

import lombok.Data;

@Data
public class AgentRecommendRequest {
    private String question;
    private Integer limit;
}
