package com.lzz.backend.config;

import com.lzz.backend.common.GlobalConstant;
import com.lzz.backend.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long userId = SessionUtil.requireUserId(request.getSession(false));
        request.setAttribute(GlobalConstant.SESSION_USER_ID, userId);
        return true;
    }
}
