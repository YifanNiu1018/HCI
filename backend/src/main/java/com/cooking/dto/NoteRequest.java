package com.cooking.dto;

import lombok.Data;

import java.util.List;

@Data
public class NoteRequest {
    private String name;

    private String description;

    private String image;

    private List<String> ingredients;

    private List<String> steps;

    private List<String> tags;

    private Boolean isPublic = true;

    private Boolean isDraft = false;
}

