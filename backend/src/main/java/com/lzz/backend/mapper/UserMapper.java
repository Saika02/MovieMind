package com.lzz.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzz.backend.entity.User;

public interface UserMapper extends BaseMapper<User> {
    User selectById(Long id);
    User selectByUsername(String username);
    User selectAnyByUsername(String username);
    int updateAvatar(Long userId, String avatarUrl);
}
