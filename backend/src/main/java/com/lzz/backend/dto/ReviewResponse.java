package com.lzz.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private String moviePosterFile;
    private Long userId;
    private String username;
    private String avatarUrl;
    private BigDecimal score;
    private String content;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
