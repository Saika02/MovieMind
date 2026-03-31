package com.lzz.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CurrentUserResponse {
    private Long userId;
    private String username;
    private Integer role;
    private String avatarUrl;
    private String bio;
}
