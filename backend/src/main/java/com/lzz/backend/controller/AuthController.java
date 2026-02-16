package com.lzz.backend.controller;

import com.lzz.backend.common.GlobalConstant;
import com.lzz.backend.dto.ApiResponse;
import com.lzz.backend.dto.AuthResponse;
import com.lzz.backend.dto.LoginRequest;
import com.lzz.backend.dto.RegisterRequest;
import com.lzz.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证", description = "登录与注册")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public ApiResponse<AuthResponse> register(@RequestBody RegisterRequest request, HttpSession session) {
        AuthResponse response = authService.register(request);
        bindSession(session, response);
        return ApiResponse.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest request, HttpSession session) {
        AuthResponse response = authService.login(request);
        bindSession(session, response);
        return ApiResponse.ok(response);
    }

    private void bindSession(HttpSession session, AuthResponse response) {
        session.setAttribute(GlobalConstant.SESSION_USER_ID, response.getUserId());
        session.setAttribute(GlobalConstant.SESSION_USERNAME, response.getUsername());
        session.setAttribute(GlobalConstant.SESSION_ROLE, response.getRole());
    }
}
