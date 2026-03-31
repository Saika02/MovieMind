package com.lzz.backend.controller;

import com.lzz.backend.dto.ApiResponse;
import com.lzz.backend.dto.PageResponse;
import com.lzz.backend.dto.ReviewCreateRequest;
import com.lzz.backend.dto.ReviewResponse;
import com.lzz.backend.dto.ReviewUpdateRequest;
import com.lzz.backend.service.ReviewService;
import com.lzz.backend.util.SessionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "点评", description = "用户点评")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @Operation(summary = "新增点评")
    public ApiResponse<ReviewResponse> create(@RequestBody ReviewCreateRequest request, HttpServletRequest httpRequest) {
        Long userId = SessionUtil.requireUserId(httpRequest);
        return ApiResponse.ok(reviewService.create(userId, request));
    }

    @GetMapping("/detail")
    @Operation(summary = "查询我的点评详情")
    public ApiResponse<ReviewResponse> get(@RequestParam Long id, HttpServletRequest httpRequest) {
        Long userId = SessionUtil.requireUserId(httpRequest);
        return ApiResponse.ok(reviewService.get(userId, id));
    }

    @GetMapping
    @Operation(summary = "列表查询我的点评")
    public ApiResponse<List<ReviewResponse>> list(@RequestParam(required = false) Long movieId, HttpServletRequest httpRequest) {
        Long userId = SessionUtil.requireUserId(httpRequest);
        return ApiResponse.ok(reviewService.list(userId, movieId));
    }

    @GetMapping("/movie")
    @Operation(summary = "列表查询电影公共点评")
    public ApiResponse<List<ReviewResponse>> listMovieReviews(@RequestParam Long movieId, HttpServletRequest httpRequest) {
        SessionUtil.requireUserId(httpRequest);
        return ApiResponse.ok(reviewService.listMovieReviews(movieId));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询我的点评")
    public ApiResponse<PageResponse<ReviewResponse>> page(@RequestParam int page, @RequestParam int size, @RequestParam(required = false) Long movieId, HttpServletRequest httpRequest) {
        Long userId = SessionUtil.requireUserId(httpRequest);
        return ApiResponse.ok(reviewService.listPage(userId, movieId, page, size));
    }

    @GetMapping("/movie/page")
    @Operation(summary = "分页查询电影公共点评")
    public ApiResponse<PageResponse<ReviewResponse>> pageMovieReviews(@RequestParam Long movieId, @RequestParam int page, @RequestParam int size, HttpServletRequest httpRequest) {
        SessionUtil.requireUserId(httpRequest);
        return ApiResponse.ok(reviewService.listMovieReviewsPage(movieId, page, size));
    }

    @PutMapping("/update")
    @Operation(summary = "更新点评")
    public ApiResponse<ReviewResponse> update(@RequestParam Long id, @RequestBody ReviewUpdateRequest request, HttpServletRequest httpRequest) {
        Long userId = SessionUtil.requireUserId(httpRequest);
        return ApiResponse.ok(reviewService.update(userId, id, request));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除点评")
    public ApiResponse<Void> delete(@RequestParam Long id, HttpServletRequest httpRequest) {
        Long userId = SessionUtil.requireUserId(httpRequest);
        reviewService.delete(userId, id);
        return ApiResponse.ok(null);
    }
}
