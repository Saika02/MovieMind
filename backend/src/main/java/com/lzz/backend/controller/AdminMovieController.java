package com.lzz.backend.controller;

import com.lzz.backend.dto.AdminMovieUpsertRequest;
import com.lzz.backend.dto.ApiResponse;
import com.lzz.backend.dto.MovieDetailResponse;
import com.lzz.backend.dto.MovieListResponse;
import com.lzz.backend.dto.PageResponse;
import com.lzz.backend.service.AdminMovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/movies")
@Tag(name = "管理员电影", description = "管理员维护电影")
public class AdminMovieController {
    private final AdminMovieService adminMovieService;

    public AdminMovieController(AdminMovieService adminMovieService) {
        this.adminMovieService = adminMovieService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "管理员查询电影详情")
    public ApiResponse<MovieDetailResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(adminMovieService.get(id));
    }

    @GetMapping("/page")
    @Operation(summary = "管理员分页查询电影")
    public ApiResponse<PageResponse<MovieListResponse>> page(@RequestParam int page,
                                                             @RequestParam int size,
                                                             @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(adminMovieService.page(keyword, page, size));
    }

    @PostMapping
    @Operation(summary = "管理员新增电影")
    public ApiResponse<MovieDetailResponse> create(@RequestBody AdminMovieUpsertRequest request) {
        return ApiResponse.ok(adminMovieService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "管理员修改电影")
    public ApiResponse<MovieDetailResponse> update(@PathVariable Long id, @RequestBody AdminMovieUpsertRequest request) {
        return ApiResponse.ok(adminMovieService.update(id, request));
    }

    @PostMapping("/upload-poster")
    @Operation(summary = "管理员上传电影海报")
    public ApiResponse<String> uploadPoster(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(adminMovieService.uploadPoster(file));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "管理员删除电影")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminMovieService.delete(id);
        return ApiResponse.ok(null);
    }
}
