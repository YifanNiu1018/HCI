package com.cooking.dto;

import com.cooking.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private UserResponse author;
    private Long parentId;
    private List<CommentResponse> replies;

    public static CommentResponse from(Comment comment) {
        List<CommentResponse> replies = comment.getReplies().stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());

        return new CommentResponse(
            comment.getId(),
            comment.getContent(),
            comment.getCreatedAt(),
            UserResponse.from(comment.getUser()),
            comment.getParent() != null ? comment.getParent().getId() : null,
            replies
        );
    }
}

