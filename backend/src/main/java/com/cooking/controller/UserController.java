package com.cooking.controller;

import com.cooking.dto.DishResponse;
import com.cooking.dto.NoteResponse;
import com.cooking.dto.UpdateUserRequest;
import com.cooking.dto.UserResponse;
import com.cooking.entity.User;
import com.cooking.repository.UserRepository;
import com.cooking.service.DishService;
import com.cooking.service.FollowService;
import com.cooking.service.NoteService;
import com.cooking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final DishService dishService;
    private final NoteService noteService;
    private final FollowService followService;
    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<UserResponse> getUserInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<DishResponse>> getFavorites() {
        try {
            List<DishResponse> favorites = dishService.getFavorites();
            return ResponseEntity.ok(favorites);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/favorite-notes")
    public ResponseEntity<List<NoteResponse>> getFavoriteNotes() {
        try {
            List<NoteResponse> favoriteNotes = noteService.getFavoriteNotes();
            return ResponseEntity.ok(favoriteNotes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            // 检查当前用户是否关注了该用户
            Boolean isFollowing = followService.isFollowing(userId);
            return ResponseEntity.ok(UserResponse.from(user, isFollowing));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{userId}/notes")
    public ResponseEntity<List<NoteResponse>> getUserPublicNotes(@PathVariable Long userId) {
        try {
            List<NoteResponse> notes = noteService.getUserPublicNotes(userId);
            return ResponseEntity.ok(notes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/info")
    public ResponseEntity<UserResponse> updateUserInfo(@Valid @RequestBody UpdateUserRequest request) {
        try {
            UserResponse response = userService.updateUser(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

