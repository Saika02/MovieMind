package com.lzz.backend.service;

import com.lzz.backend.dto.CurrentUserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    CurrentUserResponse getCurrentUser(Long userId);
    void updateAvatar(Long userId, String avatarUrl);
    String uploadAvatar(Long userId, MultipartFile file);
}
