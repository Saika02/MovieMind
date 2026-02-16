package com.lzz.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    void updateAvatar(Long userId, String avatarUrl);
    String uploadAvatar(Long userId, MultipartFile file);
}
