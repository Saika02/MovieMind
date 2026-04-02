package com.lzz.backend.controller;

import com.lzz.backend.dto.ApiResponse;
import com.lzz.backend.dto.PageResponse;
import com.lzz.backend.dto.ReviewResponse;
import com.lzz.backend.service.AdminReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reviews")
@Tag(name = "管理员点评", description = "管理员维护点评")
public class AdminReviewController {
    private final AdminReviewService adminReviewService;

    public AdminReviewController(AdminReviewService adminReviewService) {
        this.adminReviewService = adminReviewService;
    }

    @GetMapping("/page")
    @Operation(summary = "管理员分页查询点评")
    public ApiResponse<PageResponse<ReviewResponse>> page(@RequestParam int page,
                                                          @RequestParam int size,
                                                          @RequestParam(required = false) Long movieId,
                                                          @RequestParam(required = false) Long userId,
                                                          @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(adminReviewService.page(movieId, userId, keyword, page, size));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "管理员删除点评")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminReviewService.delete(id);
        return ApiResponse.ok(null);
    }
}
