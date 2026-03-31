package com.lzz.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FavoriteStatusResponse {
    private Long movieId;
    private boolean favorited;
    private Long favoriteId;
}
