package com.cooking.controller;

import com.cooking.dto.DishResponse;
import com.cooking.dto.NoteResponse;
import com.cooking.dto.UserResponse;
import com.cooking.entity.User;
import com.cooking.repository.UserRepository;
import com.cooking.service.DishService;
import com.cooking.service.NoteService;
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
}

