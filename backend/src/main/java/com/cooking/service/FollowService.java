package com.cooking.service;

import com.cooking.dto.UserResponse;
import com.cooking.entity.User;
import com.cooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final UserRepository userRepository;

    @Transactional
    public void followUser(Long userId) {
        String username = getCurrentUsername();
        if (username == null) {
            throw new RuntimeException("请先登录");
        }

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (currentUser.getId().equals(userId)) {
            throw new RuntimeException("不能关注自己");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("目标用户不存在"));

        // 检查是否已经关注（通过ID比较）
        boolean alreadyFollowing = currentUser.getFollowing().stream()
                .anyMatch(u -> u.getId().equals(userId));
        
        if (alreadyFollowing) {
            throw new RuntimeException("已经关注过该用户");
        }

        currentUser.getFollowing().add(targetUser);
        userRepository.save(currentUser);
    }

    @Transactional
    public void unfollowUser(Long userId) {
        String username = getCurrentUsername();
        if (username == null) {
            throw new RuntimeException("请先登录");
        }

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 检查是否已关注（通过ID比较）
        boolean isFollowing = currentUser.getFollowing().stream()
                .anyMatch(u -> u.getId().equals(userId));
        
        if (!isFollowing) {
            throw new RuntimeException("未关注该用户");
        }

        // 通过ID找到并移除
        currentUser.getFollowing().removeIf(u -> u.getId().equals(userId));
        userRepository.save(currentUser);
    }

    public List<UserResponse> getFollowing(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return user.getFollowing().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getFollowers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return user.getFollowers().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getMyFollowing() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new RuntimeException("请先登录");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return user.getFollowing().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getMyFollowers() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new RuntimeException("请先登录");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return user.getFollowers().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    public boolean isFollowing(Long userId) {
        String username = getCurrentUsername();
        if (username == null) {
            return false;
        }

        User currentUser = userRepository.findByUsername(username)
                .orElse(null);
        if (currentUser == null) {
            return false;
        }

        // 通过ID比较
        return currentUser.getFollowing().stream()
                .anyMatch(u -> u.getId().equals(userId));
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
