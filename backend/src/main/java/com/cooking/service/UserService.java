package com.cooking.service;

import com.cooking.dto.UpdateUserRequest;
import com.cooking.dto.UserResponse;
import com.cooking.entity.User;
import com.cooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse updateUser(UpdateUserRequest request) {
        String username = getCurrentUsername();
        if (username == null) {
            throw new RuntimeException("请先登录");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 更新用户名
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            String newUsername = request.getUsername().trim();
            // 检查新用户名是否已被其他用户使用
            if (!newUsername.equals(user.getUsername()) && userRepository.existsByUsername(newUsername)) {
                throw new RuntimeException("用户名已存在");
            }
            user.setUsername(newUsername);
        }

        // 更新邮箱
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String newEmail = request.getEmail().trim();
            // 检查新邮箱是否已被其他用户使用
            if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("邮箱已被注册");
            }
            user.setEmail(newEmail);
        }

        // 更新头像
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        user = userRepository.save(user);
        return UserResponse.from(user);
    }

    private String getCurrentUsername() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if (username == null || username.equals("anonymousUser")) {
                return null;
            }
            return username;
        } catch (Exception e) {
            return null;
        }
    }
}

