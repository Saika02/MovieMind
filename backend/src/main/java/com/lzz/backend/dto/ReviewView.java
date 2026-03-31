package com.lzz.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class ReviewView {
    private Long id;
    private Long userId;
    private Long movieId;
    private String movieTitle;
    private String moviePosterFile;
    private BigDecimal score;
    private String content;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String username;
    private String avatarUrl;
}
