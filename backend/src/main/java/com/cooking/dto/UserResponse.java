package com.cooking.dto;

import com.cooking.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private Integer followingCount;
    private Integer followersCount;
    private Boolean isFollowing;

    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getAvatar(),
            user.getFollowing() != null ? user.getFollowing().size() : 0,
            user.getFollowers() != null ? user.getFollowers().size() : 0,
            null
        );
    }

    public static UserResponse from(User user, Boolean isFollowing) {
        UserResponse response = from(user);
        response.setIsFollowing(isFollowing);
        return response;
    }
}

