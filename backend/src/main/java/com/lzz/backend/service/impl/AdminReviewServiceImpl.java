package com.lzz.backend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzz.backend.dto.PageResponse;
import com.lzz.backend.dto.ReviewResponse;
import com.lzz.backend.dto.ReviewView;
import com.lzz.backend.exception.ServiceException;
import com.lzz.backend.mapper.ReviewMapper;
import com.lzz.backend.service.AdminReviewService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminReviewServiceImpl implements AdminReviewService {
    private final ReviewMapper reviewMapper;

    public AdminReviewServiceImpl(ReviewMapper reviewMapper) {
        this.reviewMapper = reviewMapper;
    }

    @Override
    public PageResponse<ReviewResponse> page(Long movieId, Long userId, String keyword, int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new ServiceException("分页参数不合法");
        }
        String normalizedKeyword = normalizeKeyword(keyword);
        Page<ReviewView> pageData = reviewMapper.selectAdminViewPage(new Page<>(page, size), movieId, userId, normalizedKeyword);
        List<ReviewResponse> items = pageData.getRecords().stream()
                .map(this::toResponse)
                .toList();
        return new PageResponse<>(page, size, pageData.getTotal(), items);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new ServiceException("点评 ID 不能为空");
        }
        int updated = reviewMapper.adminSoftDelete(id);
        if (updated == 0) {
            throw new ServiceException("点评记录不存在");
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String normalized = keyword.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private ReviewResponse toResponse(ReviewView review) {
        return new ReviewResponse(
                review.getId(),
                review.getMovieId(),
                review.getMovieTitle(),
                review.getMoviePosterFile(),
                review.getUserId(),
                review.getUsername(),
                review.getAvatarUrl(),
                review.getScore(),
                review.getContent(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
