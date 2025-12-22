package com.cooking.dto;

import com.cooking.entity.Note;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponse {
    private Long id;
    private String name;
    private String description;
    private String image;
    private List<String> ingredients;
    private List<String> steps;
    private List<String> tags;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private UserResponse author;
    private Boolean isFavorite;
    private Boolean isDraft;

    public static NoteResponse from(Note note, Boolean isFavorite) {
        NoteResponse response = new NoteResponse();
        response.setId(note.getId());
        response.setName(note.getName());
        response.setDescription(note.getDescription());
        response.setImage(note.getImage());
        response.setIngredients(note.getIngredients());
        response.setSteps(note.getSteps());
        response.setTags(note.getTags());
        response.setIsPublic(note.getIsPublic());
        response.setCreatedAt(note.getCreatedAt());
        response.setAuthor(UserResponse.from(note.getUser()));
        response.setIsFavorite(isFavorite != null ? isFavorite : false);
        response.setIsDraft(note.getIsDraft() != null ? note.getIsDraft() : false);
        return response;
    }

    public static NoteResponse from(Note note) {
        return from(note, false);
    }
}

