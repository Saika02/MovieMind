package com.lzz.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReviewUpdateRequest {
    private BigDecimal score;
    private String content;
}
