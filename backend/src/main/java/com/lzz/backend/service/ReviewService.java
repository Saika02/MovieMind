package com.lzz.backend.service;

import com.lzz.backend.dto.ReviewCreateRequest;
import com.lzz.backend.dto.ReviewResponse;
import com.lzz.backend.dto.ReviewUpdateRequest;
import com.lzz.backend.dto.PageResponse;

import java.util.List;

public interface ReviewService {
    ReviewResponse create(Long userId, ReviewCreateRequest request);
    ReviewResponse get(Long userId, Long id);
    List<ReviewResponse> list(Long userId, Long movieId);
    PageResponse<ReviewResponse> listPage(Long userId, Long movieId, int page, int size);
    ReviewResponse update(Long userId, Long id, ReviewUpdateRequest request);
    void delete(Long userId, Long id);
}
