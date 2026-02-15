package com.lzz.backend.controller;

import com.lzz.backend.common.GlobalConstant;
import com.lzz.backend.dto.ApiResponse;
import com.lzz.backend.dto.AuthResponse;
import com.lzz.backend.dto.LoginRequest;
import com.lzz.backend.dto.RegisterRequest;
import com.lzz.backend.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@RequestBody RegisterRequest request, HttpSession session) {
        AuthResponse response = authService.register(request);
        bindSession(session, response);
        return ApiResponse.ok(response);
    }

    @PostMapping("/login")
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
