package com.lzz.backend.service;

import com.lzz.backend.dto.FavoriteCreateRequest;
import com.lzz.backend.dto.FavoriteResponse;
import com.lzz.backend.dto.PageResponse;
import java.util.List;

public interface FavoriteService {
    FavoriteResponse create(Long userId, FavoriteCreateRequest request);
    FavoriteResponse get(Long userId, Long id);
    List<FavoriteResponse> list(Long userId);
    PageResponse<FavoriteResponse> listPage(Long userId, int page, int size);
    void delete(Long userId, Long id);
}
