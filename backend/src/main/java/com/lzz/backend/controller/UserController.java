package com.lzz.backend.controller;

import com.lzz.backend.dto.ApiResponse;
import com.lzz.backend.dto.CurrentUserResponse;
import com.lzz.backend.service.UserService;
import com.lzz.backend.util.SessionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户", description = "用户资料")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public ApiResponse<CurrentUserResponse> me(HttpServletRequest request) {
        Long userId = SessionUtil.requireUserId(request);
        return ApiResponse.ok(userService.getCurrentUser(userId));
    }

    @PostMapping("/me/avatar")
    @Operation(summary = "上传头像")
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = SessionUtil.requireUserId(request);
        return ApiResponse.ok(userService.uploadAvatar(userId, file));
    }
}
