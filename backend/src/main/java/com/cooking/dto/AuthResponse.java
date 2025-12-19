package com.cooking.dto;

import com.cooking.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserResponse user;

    public static AuthResponse from(User user, String token) {
        return new AuthResponse(token, UserResponse.from(user));
    }
}

