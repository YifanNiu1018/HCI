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

    public static NoteResponse from(Note note) {
        return new NoteResponse(
            note.getId(),
            note.getName(),
            note.getDescription(),
            note.getImage(),
            note.getIngredients(),
            note.getSteps(),
            note.getTags(),
            note.getIsPublic(),
            note.getCreatedAt(),
            UserResponse.from(note.getUser())
        );
    }
}

