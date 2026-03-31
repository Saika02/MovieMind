package com.lzz.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthSessionResponse {
    private boolean loggedIn;
    private Long userId;
    private String username;
    private Integer role;
}
