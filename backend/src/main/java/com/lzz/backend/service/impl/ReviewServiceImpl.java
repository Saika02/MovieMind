package com.lzz.backend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzz.backend.dto.PageResponse;
import com.lzz.backend.dto.ReviewCreateRequest;
import com.lzz.backend.dto.ReviewResponse;
import com.lzz.backend.dto.ReviewUpdateRequest;
import com.lzz.backend.dto.ReviewView;
import com.lzz.backend.entity.Review;
import com.lzz.backend.exception.ServiceException;
import com.lzz.backend.mapper.MovieMapper;
import com.lzz.backend.mapper.ReviewMapper;
import com.lzz.backend.service.ReviewService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewMapper reviewMapper;
    private final MovieMapper movieMapper;

    public ReviewServiceImpl(ReviewMapper reviewMapper, MovieMapper movieMapper) {
        this.reviewMapper = reviewMapper;
        this.movieMapper = movieMapper;
    }

    @Override
    @Transactional
    public ReviewResponse create(Long userId, ReviewCreateRequest request) {
        if (request == null || request.getMovieId() == null || request.getScore() == null || request.getContent() == null) {
            throw new ServiceException("参数不完整");
        }
        Review activeReview = reviewMapper.selectByUserAndMovie(userId, request.getMovieId());
        if (activeReview != null) {
            throw new ServiceException("已存在点评记录");
        }
        Review deletedReview = reviewMapper.selectAnyByUserAndMovie(userId, request.getMovieId());
        if (deletedReview != null) {
            reviewMapper.restoreReview(deletedReview.getId(), userId, request.getScore(), request.getContent());
            refreshMovieSiteVoteStats(request.getMovieId());
            return toResponse(reviewMapper.selectViewByIdAndUser(deletedReview.getId(), userId));
        }
        Review review = new Review();
        review.setUserId(userId);
        review.setMovieId(request.getMovieId());
        review.setScore(request.getScore());
        review.setContent(request.getContent());
        try {
            reviewMapper.insert(review);
        } catch (DuplicateKeyException ex) {
            throw new ServiceException("已存在点评记录");
        }
        refreshMovieSiteVoteStats(review.getMovieId());
        return toResponse(reviewMapper.selectViewByIdAndUser(review.getId(), userId));
    }

    @Override
    public ReviewResponse get(Long userId, Long id) {
        Review review = reviewMapper.selectByIdAndUser(id, userId);
        if (review == null) {
            throw new ServiceException("点评记录不存在");
        }
        return toResponse(reviewMapper.selectViewByIdAndUser(id, userId));
    }

    @Override
    public List<ReviewResponse> list(Long userId, Long movieId) {
        List<ReviewView> reviews = movieId == null
                ? reviewMapper.selectViewsByUser(userId)
                : reviewMapper.selectViewsByUserAndMovie(userId, movieId);
        return reviews.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<ReviewResponse> listPage(Long userId, Long movieId, int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new ServiceException("分页参数不合法");
        }
        Page<ReviewView> pageData = movieId == null
                ? reviewMapper.selectViewPageByUser(new Page<>(page, size), userId)
                : reviewMapper.selectViewPageByUserAndMovie(new Page<>(page, size), userId, movieId);
        List<ReviewResponse> items = pageData.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new PageResponse<>(page, size, pageData.getTotal(), items);
    }

    @Override
    public List<ReviewResponse> listMovieReviews(Long movieId) {
        if (movieId == null) {
            throw new ServiceException("电影 ID 不能为空");
        }
        return reviewMapper.selectViewsByMovie(movieId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<ReviewResponse> listMovieReviewsPage(Long movieId, int page, int size) {
        if (movieId == null) {
            throw new ServiceException("电影 ID 不能为空");
        }
        if (page < 1 || size < 1 || size > 100) {
            throw new ServiceException("分页参数不合法");
        }
        Page<ReviewView> pageData = reviewMapper.selectViewPageByMovie(new Page<>(page, size), movieId);
        List<ReviewResponse> items = pageData.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new PageResponse<>(page, size, pageData.getTotal(), items);
    }

    @Override
    @Transactional
    public ReviewResponse update(Long userId, Long id, ReviewUpdateRequest request) {
        if (request == null || request.getScore() == null || request.getContent() == null) {
            throw new ServiceException("参数不完整");
        }
        Review existing = reviewMapper.selectByIdAndUser(id, userId);
        if (existing == null) {
            throw new ServiceException("点评记录不存在");
        }
        int updated = reviewMapper.updateReview(id, userId, request.getScore(), request.getContent());
        if (updated == 0) {
            throw new ServiceException("点评记录不存在");
        }
        refreshMovieSiteVoteStats(existing.getMovieId());
        return toResponse(reviewMapper.selectViewByIdAndUser(id, userId));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        Review existing = reviewMapper.selectByIdAndUser(id, userId);
        if (existing == null) {
            throw new ServiceException("点评记录不存在");
        }
        int updated = reviewMapper.softDelete(id, userId);
        if (updated == 0) {
            throw new ServiceException("点评记录不存在");
        }
        refreshMovieSiteVoteStats(existing.getMovieId());
    }

    private void refreshMovieSiteVoteStats(Long movieId) {
        if (movieId == null) {
            throw new ServiceException("电影 ID 不能为空");
        }
        movieMapper.refreshSiteVoteStats(movieId);
    }

    private ReviewResponse toResponse(ReviewView review) {
        if (review == null) {
            throw new ServiceException("点评记录不存在");
        }
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
