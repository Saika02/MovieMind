package com.lzz.backend.controller;

import com.lzz.backend.dto.ApiResponse;
import com.lzz.backend.dto.FavoriteCreateRequest;
import com.lzz.backend.dto.FavoriteResponse;
import com.lzz.backend.dto.PageResponse;
import com.lzz.backend.service.FavoriteService;
import com.lzz.backend.util.SessionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "收藏", description = "收藏/想看")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping
    @Operation(summary = "新增收藏/想看")
    public ApiResponse<FavoriteResponse> create(@RequestBody FavoriteCreateRequest request, HttpServletRequest httpRequest) {
        Long userId = SessionUtil.requireUserId(httpRequest);
        return ApiResponse.ok(favoriteService.create(userId, request));
    }

    @GetMapping("/detail")
    @Operation(summary = "查询收藏/想看")
    public ApiResponse<FavoriteResponse> get(@RequestParam Long id, HttpServletRequest httpRequest) {
        Long userId = SessionUtil.requireUserId(httpRequest);
        return ApiResponse.ok(favoriteService.get(userId, id));
    }

    @GetMapping
    @Operation(summary = "列表查询收藏/想看")
    public ApiResponse<List<FavoriteResponse>> list(HttpServletRequest httpRequest) {
        Long userId = SessionUtil.requireUserId(httpRequest);
        return ApiResponse.ok(favoriteService.list(userId));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询收藏/想看")
    public ApiResponse<PageResponse<FavoriteResponse>> page(@RequestParam int page, @RequestParam int size, HttpServletRequest httpRequest) {
        Long userId = SessionUtil.requireUserId(httpRequest);
        return ApiResponse.ok(favoriteService.listPage(userId, page, size));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除收藏/想看")
    public ApiResponse<Void> delete(@RequestParam Long id, HttpServletRequest httpRequest) {
        Long userId = SessionUtil.requireUserId(httpRequest);
        favoriteService.delete(userId, id);
        return ApiResponse.ok(null);
    }
}
