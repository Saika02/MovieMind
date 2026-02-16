package com.lzz.backend.service.impl;

import com.lzz.backend.exception.ServiceException;
import com.lzz.backend.mapper.UserMapper;
import com.lzz.backend.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final String uploadBaseDir;

    public UserServiceImpl(UserMapper userMapper, @Value("${app.upload-base-dir:${user.dir}/uploads}") String uploadBaseDir) {
        this.userMapper = userMapper;
        this.uploadBaseDir = uploadBaseDir;
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        int updated = userMapper.updateAvatar(userId, avatarUrl);
        if (updated == 0) {
            throw new ServiceException("用户不存在");
        }
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ServiceException("仅支持图片文件");
        }
        String original = file.getOriginalFilename();
        String extension = "";
        if (original != null) {
            int dot = original.lastIndexOf('.');
            if (dot >= 0) {
                extension = original.substring(dot);
            }
        }
        String filename = userId + "_" + UUID.randomUUID() + extension;
        try {
            Path dir = Paths.get(uploadBaseDir).resolve("avatars").toAbsolutePath();
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            file.transferTo(target.toFile());
        } catch (Exception e) {
            throw new ServiceException("上传失败");
        }
        String url = "/uploads/avatars/" + filename;
        updateAvatar(userId, url);
        return url;
    }
}
