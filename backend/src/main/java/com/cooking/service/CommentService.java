package com.cooking.service;

import com.cooking.dto.CommentRequest;
import com.cooking.dto.CommentResponse;
import com.cooking.entity.Comment;
import com.cooking.entity.Dish;
import com.cooking.entity.Note;
import com.cooking.entity.User;
import com.cooking.repository.CommentRepository;
import com.cooking.repository.DishRepository;
import com.cooking.repository.NoteRepository;
import com.cooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final DishRepository dishRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse createComment(Long dishId, CommentRequest request) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("菜品不存在"));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUser(user);
        comment.setDish(dish);

        // 如果是回复
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("父评论不存在"));
            comment.setParent(parent);
        }

        comment = commentRepository.save(comment);
        return CommentResponse.from(comment);
    }

    @Transactional
    public CommentResponse createNoteComment(Long noteId, CommentRequest request) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));

        // 只能评论公开的笔记
        if (!note.getIsPublic()) {
            throw new RuntimeException("只能评论公开的笔记");
        }

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUser(user);
        comment.setNote(note);

        // 如果是回复
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("父评论不存在"));
            comment.setParent(parent);
        }

        comment = commentRepository.save(comment);
        return CommentResponse.from(comment);
    }

    public List<CommentResponse> getCommentsByDish(Long dishId) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("菜品不存在"));

        List<Comment> topLevelComments = commentRepository.findByDishAndParentIsNullOrderByCreatedAtDesc(dish);
        return topLevelComments.stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    public List<CommentResponse> getCommentsByNote(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));

        List<Comment> topLevelComments = commentRepository.findByNoteAndParentIsNullOrderByCreatedAtDesc(note);
        return topLevelComments.stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        // 只能删除自己的评论
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权删除此评论");
        }

        commentRepository.delete(comment);
    }

    private String getCurrentUsername() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if (username == null || username.equals("anonymousUser")) {
                throw new RuntimeException("请先登录");
            }
            return username;
        } catch (Exception e) {
            throw new RuntimeException("请先登录");
        }
    }
}

