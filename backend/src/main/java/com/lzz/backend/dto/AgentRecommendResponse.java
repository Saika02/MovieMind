package com.lzz.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AgentRecommendResponse {
    private String answer;
    private List<MovieListResponse> movies;
}
