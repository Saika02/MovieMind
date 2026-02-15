package com.lzz.backend.service;

import com.lzz.backend.dto.AuthResponse;
import com.lzz.backend.dto.LoginRequest;
import com.lzz.backend.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
