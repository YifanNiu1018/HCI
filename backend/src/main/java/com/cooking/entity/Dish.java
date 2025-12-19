package com.cooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dishes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String image;

    @ElementCollection
    @CollectionTable(name = "dish_ingredients", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "ingredient")
    private List<String> ingredients = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "dish_steps", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "step", length = 2000)
    private List<String> steps = new ArrayList<>();

    private String category;

    private String difficulty;

    private Integer cookingTime;

    private Integer servings;
}

