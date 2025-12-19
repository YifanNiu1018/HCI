package com.cooking.controller;

import com.cooking.dto.DishResponse;
import com.cooking.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @GetMapping
    public ResponseEntity<List<DishResponse>> getAllDishes(
            @RequestParam(required = false) String keyword) {
        List<DishResponse> dishes = dishService.getAllDishes(keyword);
        return ResponseEntity.ok(dishes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DishResponse> getDishById(@PathVariable Long id) {
        try {
            DishResponse dish = dishService.getDishById(id);
            return ResponseEntity.ok(dish);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Map<String, Boolean>> toggleFavorite(@PathVariable Long id) {
        try {
            DishResponse dish = dishService.toggleFavorite(id);
            return ResponseEntity.ok(Map.of("isFavorite", dish.getIsFavorite()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

