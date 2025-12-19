package com.cooking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class NoteRequest {
    @NotBlank(message = "菜名不能为空")
    private String name;

    private String description;

    private String image;

    private List<String> ingredients;

    private List<String> steps;

    private List<String> tags;

    private Boolean isPublic = true;
}

