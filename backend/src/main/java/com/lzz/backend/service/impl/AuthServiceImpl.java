package com.lzz.backend.service.impl;

import com.lzz.backend.common.GlobalConstant;
import com.lzz.backend.dto.AuthResponse;
import com.lzz.backend.dto.AuthSessionResponse;
import com.lzz.backend.dto.LoginRequest;
import com.lzz.backend.dto.RegisterRequest;
import com.lzz.backend.entity.User;
import com.lzz.backend.exception.ServiceException;
import com.lzz.backend.mapper.UserMapper;
import com.lzz.backend.service.AuthService;
import com.lzz.backend.util.PasswordHasher;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;

    public AuthServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String username = normalize(request.getUsername());
        String password = normalize(request.getPassword());
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new ServiceException("用户名或密码不能为空");
        }

        User existing = userMapper.selectAnyByUsername(username);
        if (existing != null) {
            throw new ServiceException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordHasher.hash(password));
        user.setRole(GlobalConstant.ROLE_USER);
        userMapper.insert(user);

        return new AuthResponse(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String username = normalize(request.getUsername());
        String password = normalize(request.getPassword());
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new ServiceException("用户名或密码不能为空");
        }

        User user = userMapper.selectByUsername(username);
        if (user == null || !PasswordHasher.verify(password, user.getPasswordHash())) {
            throw new ServiceException("用户名或密码错误");
        }

        return new AuthResponse(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public AuthSessionResponse getSession(HttpSession session) {
        if (session == null) {
            return new AuthSessionResponse(false, null, null, null);
        }
        Object userId = session.getAttribute(GlobalConstant.SESSION_USER_ID);
        Object username = session.getAttribute(GlobalConstant.SESSION_USERNAME);
        Object role = session.getAttribute(GlobalConstant.SESSION_ROLE);
        if (userId == null || username == null || role == null) {
            return new AuthSessionResponse(false, null, null, null);
        }
        return new AuthSessionResponse(true, parseLong(userId), String.valueOf(username), parseInteger(role));
    }

    @Override
    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }

    private Long parseLong(Object value) {
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }
        if (value instanceof String text) {
            return Long.valueOf(text);
        }
        return null;
    }

    private Integer parseInteger(Object value) {
        if (value instanceof Integer intValue) {
            return intValue;
        }
        if (value instanceof Long longValue) {
            return longValue.intValue();
        }
        if (value instanceof String text) {
            return Integer.valueOf(text);
        }
        return null;
    }
}
