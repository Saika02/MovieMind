package com.lzz.backend.service;

import com.lzz.backend.dto.AuthResponse;
import com.lzz.backend.dto.AuthSessionResponse;
import com.lzz.backend.dto.LoginRequest;
import com.lzz.backend.dto.RegisterRequest;
import jakarta.servlet.http.HttpSession;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthSessionResponse getSession(HttpSession session);
    void logout(HttpSession session);
}
