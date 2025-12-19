package com.cooking.controller;

import com.cooking.dto.CommentRequest;
import com.cooking.dto.CommentResponse;
import com.cooking.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dishes/{dishId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long dishId,
            @Valid @RequestBody CommentRequest request) {
        try {
            CommentResponse response = commentService.createComment(dishId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long dishId) {
        try {
            List<CommentResponse> comments = commentService.getCommentsByDish(dishId);
            return ResponseEntity.ok(comments);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

