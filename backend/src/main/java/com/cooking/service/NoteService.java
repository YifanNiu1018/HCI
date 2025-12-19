package com.cooking.service;

import com.cooking.dto.NoteRequest;
import com.cooking.dto.NoteResponse;
import com.cooking.entity.Note;
import com.cooking.entity.User;
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
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @Transactional
    public NoteResponse createNote(NoteRequest request) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Note note = new Note();
        note.setName(request.getName());
        note.setDescription(request.getDescription());
        note.setImage(request.getImage() != null ? request.getImage() : "/images/default.jpg");
        note.setIngredients(request.getIngredients() != null ? request.getIngredients() : List.of());
        note.setSteps(request.getSteps() != null ? request.getSteps() : List.of());
        note.setTags(request.getTags() != null ? request.getTags() : List.of());
        note.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : true);
        note.setUser(user);

        note = noteRepository.save(note);
        return NoteResponse.from(note);
    }

    public List<NoteResponse> getUserNotes() {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return noteRepository.findByUser(user).stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }

    public List<NoteResponse> getPublicNotes() {
        return noteRepository.findByIsPublicTrue().stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }

    public List<NoteResponse> searchPublicNotes(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getPublicNotes();
        }
        return noteRepository.searchPublicNotes(keyword.trim()).stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }

    public NoteResponse getNoteById(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));

        // 检查权限：公开笔记或自己的笔记
        String username = getCurrentUsername();
        if (!note.getIsPublic()) {
            if (username == null) {
                throw new RuntimeException("无权访问此笔记");
            }
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            if (!note.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("无权访问此笔记");
            }
        }

        return NoteResponse.from(note);
    }

    @Transactional
    public NoteResponse updateNoteVisibility(Long noteId, Boolean isPublic) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));

        if (!note.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权修改此笔记");
        }

        note.setIsPublic(isPublic);
        note = noteRepository.save(note);
        return NoteResponse.from(note);
    }

    @Transactional
    public void deleteNote(Long noteId) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));

        if (!note.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权删除此笔记");
        }

        noteRepository.delete(note);
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

