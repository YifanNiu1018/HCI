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
        note.setIsDraft(request.getIsDraft() != null ? request.getIsDraft() : false);
        note.setUser(user);

        note = noteRepository.save(note);
        return NoteResponse.from(note);
    }

    public List<NoteResponse> getUserNotes() {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return noteRepository.findByUserAndIsDraftFalse(user).stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }

    public List<NoteResponse> getDrafts() {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return noteRepository.findByUserAndIsDraftTrue(user).stream()
                .map(NoteResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public NoteResponse saveDraft(NoteRequest request) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Note note = new Note();
        note.setName(request.getName() != null && !request.getName().trim().isEmpty() 
                ? request.getName() : "未命名草稿");
        note.setDescription(request.getDescription());
        note.setImage(request.getImage() != null ? request.getImage() : "/images/default.jpg");
        note.setIngredients(request.getIngredients() != null ? request.getIngredients() : List.of());
        note.setSteps(request.getSteps() != null ? request.getSteps() : List.of());
        note.setTags(request.getTags() != null ? request.getTags() : List.of());
        note.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : true);
        note.setIsDraft(true);
        note.setUser(user);

        note = noteRepository.save(note);
        return NoteResponse.from(note);
    }

    @Transactional
    public NoteResponse updateDraft(Long draftId, NoteRequest request) {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Note note = noteRepository.findById(draftId)
                .orElseThrow(() -> new RuntimeException("草稿不存在"));

        if (!note.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权修改此草稿");
        }

        if (!note.getIsDraft()) {
            throw new RuntimeException("该笔记不是草稿");
        }

        // 更新name，如果为空字符串则保持原名称，如果为null则不更新
        if (request.getName() != null) {
            String newName = request.getName().trim();
            if (!newName.isEmpty()) {
                note.setName(newName);
            }
            // 如果为空字符串，保持原名称不变（不更新）
        }
        if (request.getDescription() != null) {
            note.setDescription(request.getDescription());
        }
        if (request.getImage() != null) {
            note.setImage(request.getImage());
        }
        if (request.getIngredients() != null) {
            note.setIngredients(request.getIngredients());
        }
        if (request.getSteps() != null) {
            note.setSteps(request.getSteps());
        }
        if (request.getTags() != null) {
            note.setTags(request.getTags());
        }
        if (request.getIsPublic() != null) {
            note.setIsPublic(request.getIsPublic());
        }
        if (request.getIsDraft() != null) {
            note.setIsDraft(request.getIsDraft());
        }

        note = noteRepository.save(note);
        return NoteResponse.from(note);
    }

    public List<NoteResponse> getPublicNotes() {
        String username = getCurrentUsername();
        return noteRepository.findByIsPublicTrueAndIsDraftFalse().stream()
                .map(note -> {
                    boolean isFavorite = username != null && isFavorite(note.getId(), username);
                    return NoteResponse.from(note, isFavorite);
                })
                .collect(Collectors.toList());
    }

    public List<NoteResponse> searchPublicNotes(String keyword) {
        String username = getCurrentUsername();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getPublicNotes();
        }
        return noteRepository.searchPublicNotes(keyword.trim()).stream()
                .map(note -> {
                    boolean isFavorite = username != null && isFavorite(note.getId(), username);
                    return NoteResponse.from(note, isFavorite);
                })
                .collect(Collectors.toList());
    }

    public NoteResponse getNoteById(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));

        // 检查权限：公开笔记或自己的笔记（草稿只能自己访问）
        String username = getCurrentUsername();
        boolean isDraft = note.getIsDraft() != null && note.getIsDraft();
        
        if (isDraft) {
            // 草稿只能自己访问
            if (username == null) {
                throw new RuntimeException("无权访问此笔记");
            }
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            if (!note.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("无权访问此笔记");
            }
        } else if (!note.getIsPublic()) {
            // 私密笔记只能自己访问
            if (username == null) {
                throw new RuntimeException("无权访问此笔记");
            }
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            if (!note.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("无权访问此笔记");
            }
        }

        boolean isFavorite = username != null && isFavorite(note.getId(), username);
        return NoteResponse.from(note, isFavorite);
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

    @Transactional
    public NoteResponse toggleFavorite(Long noteId) {
        String username = getCurrentUsername();
        if (username == null) {
            throw new RuntimeException("请先登录");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("笔记不存在"));

        // 只能收藏公开的笔记
        if (!note.getIsPublic()) {
            throw new RuntimeException("只能收藏公开的笔记");
        }

        boolean isFavorite;
        if (user.getFavoriteNotes().contains(note)) {
            user.getFavoriteNotes().remove(note);
            isFavorite = false;
        } else {
            user.getFavoriteNotes().add(note);
            isFavorite = true;
        }

        userRepository.save(user);
        return NoteResponse.from(note, isFavorite);
    }

    public List<NoteResponse> getFavoriteNotes() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new RuntimeException("请先登录");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return user.getFavoriteNotes().stream()
                .map(note -> NoteResponse.from(note, true))
                .collect(Collectors.toList());
    }

    private boolean isFavorite(Long noteId, String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null)
            return false;
        return user.getFavoriteNotes().stream()
                .anyMatch(note -> note.getId().equals(noteId));
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return null;
        }
    }
}
