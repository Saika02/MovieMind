package com.lzz.backend.controller;

import com.lzz.backend.dto.ApiResponse;
import com.lzz.backend.dto.MovieDetailResponse;
import com.lzz.backend.dto.MovieListResponse;
import com.lzz.backend.dto.PageResponse;
import com.lzz.backend.service.MovieService;
import com.lzz.backend.util.SessionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@Tag(name = "电影", description = "电影查询")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/detail")
    @Operation(summary = "查询电影详情")
    public ApiResponse<MovieDetailResponse> get(@RequestParam Long id, HttpServletRequest httpRequest) {
        SessionUtil.requireUserId(httpRequest);
        return ApiResponse.ok(movieService.get(id));
    }

    @GetMapping
    @Operation(summary = "列表查询电影")
    public ApiResponse<List<MovieListResponse>> list(@RequestParam(required = false) String keyword, HttpServletRequest httpRequest) {
        SessionUtil.requireUserId(httpRequest);
        return ApiResponse.ok(movieService.list(keyword));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询电影")
    public ApiResponse<PageResponse<MovieListResponse>> page(@RequestParam int page, @RequestParam int size, @RequestParam(required = false) String keyword, HttpServletRequest httpRequest) {
        SessionUtil.requireUserId(httpRequest);
        return ApiResponse.ok(movieService.listPage(keyword, page, size));
    }
}
