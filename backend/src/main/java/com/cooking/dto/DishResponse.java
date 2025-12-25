package com.cooking.dto;

import com.cooking.entity.Dish;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishResponse {
    private Long id;
    private String name;
    private String description;
    private String image;
    private List<String> ingredients;
    private List<String> steps;
    private String category;
    private String difficulty;
    private Integer cookingTime;
    private Integer servings;
    private Boolean isFavorite;
    private Integer calories;
    private Double protein;
    private Double fat;
    private Double carbohydrates;
    private Double fiber;

    public static DishResponse from(Dish dish, Boolean isFavorite) {
        return new DishResponse(
            dish.getId(),
            dish.getName(),
            dish.getDescription(),
            dish.getImage(),
            dish.getIngredients(),
            dish.getSteps(),
            dish.getCategory(),
            dish.getDifficulty(),
            dish.getCookingTime(),
            dish.getServings(),
            isFavorite,
            dish.getCalories(),
            dish.getProtein(),
            dish.getFat(),
            dish.getCarbohydrates(),
            dish.getFiber()
        );
    }
}

