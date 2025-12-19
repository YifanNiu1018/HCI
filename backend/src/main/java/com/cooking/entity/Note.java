package com.cooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String image;

    @ElementCollection
    @CollectionTable(name = "note_ingredients", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "ingredient")
    private List<String> ingredients = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "note_steps", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "step", length = 2000)
    private List<String> steps = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "note_tags", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isPublic = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

