package com.lzz.backend.service;

import com.lzz.backend.dto.PageResponse;
import com.lzz.backend.dto.ReviewResponse;

public interface AdminReviewService {
    PageResponse<ReviewResponse> page(Long movieId, Long userId, String keyword, int page, int size);
    void delete(Long id);
}
