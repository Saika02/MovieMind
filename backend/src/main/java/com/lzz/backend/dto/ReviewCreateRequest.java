package com.lzz.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReviewCreateRequest {
    private Long movieId;
    private BigDecimal score;
    private String content;
}
