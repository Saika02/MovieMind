package com.lzz.backend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzz.backend.dto.ReviewCreateRequest;
import com.lzz.backend.dto.ReviewResponse;
import com.lzz.backend.dto.ReviewUpdateRequest;
import com.lzz.backend.dto.PageResponse;
import com.lzz.backend.entity.Review;
import com.lzz.backend.exception.ServiceException;
import com.lzz.backend.mapper.ReviewMapper;
import com.lzz.backend.service.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewMapper reviewMapper;

    public ReviewServiceImpl(ReviewMapper reviewMapper) {
        this.reviewMapper = reviewMapper;
    }

    @Override
    public ReviewResponse create(Long userId, ReviewCreateRequest request) {
        if (request == null || request.getMovieId() == null || request.getScore() == null || request.getContent() == null) {
            throw new ServiceException("参数不完整");
        }
        Review existing = reviewMapper.selectByUserAndMovie(userId, request.getMovieId());
        if (existing != null) {
            throw new ServiceException("已存在评价");
        }
        Review review = new Review();
        review.setUserId(userId);
        review.setMovieId(request.getMovieId());
        review.setScore(request.getScore());
        review.setContent(request.getContent());
        reviewMapper.insert(review);
        return new ReviewResponse(review.getId(), review.getMovieId(), review.getScore(), review.getContent());
    }

    @Override
    public ReviewResponse get(Long userId, Long id) {
        Review review = reviewMapper.selectByIdAndUser(id, userId);
        if (review == null) {
            throw new ServiceException("记录不存在");
        }
        return new ReviewResponse(review.getId(), review.getMovieId(), review.getScore(), review.getContent());
    }

    @Override
    public List<ReviewResponse> list(Long userId, Long movieId) {
        List<Review> reviews = movieId == null
                ? reviewMapper.selectByUser(userId)
                : reviewMapper.selectByUserAndMovieList(userId, movieId);
        return reviews.stream()
                .map(item -> new ReviewResponse(item.getId(), item.getMovieId(), item.getScore(), item.getContent()))
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<ReviewResponse> listPage(Long userId, Long movieId, int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new ServiceException("分页参数不合法");
        }
        Page<Review> pageData = movieId == null
                ? reviewMapper.selectPageByUser(new Page<>(page, size), userId)
                : reviewMapper.selectPageByUserAndMovie(new Page<>(page, size), userId, movieId);
        List<ReviewResponse> items = pageData.getRecords().stream()
                .map(item -> new ReviewResponse(item.getId(), item.getMovieId(), item.getScore(), item.getContent()))
                .collect(Collectors.toList());
        return new PageResponse<>(page, size, pageData.getTotal(), items);
    }

    @Override
    public ReviewResponse update(Long userId, Long id, ReviewUpdateRequest request) {
        if (request == null || request.getScore() == null || request.getContent() == null) {
            throw new ServiceException("参数不完整");
        }
        int updated = reviewMapper.updateReview(id, userId, request.getScore(), request.getContent());
        if (updated == 0) {
            throw new ServiceException("记录不存在");
        }
        Review review = reviewMapper.selectByIdAndUser(id, userId);
        return new ReviewResponse(review.getId(), review.getMovieId(), review.getScore(), review.getContent());
    }

    @Override
    public void delete(Long userId, Long id) {
        int updated = reviewMapper.softDelete(id, userId);
        if (updated == 0) {
            throw new ServiceException("记录不存在");
        }
    }
}
