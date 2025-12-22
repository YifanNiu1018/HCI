package com.cooking.controller;

import com.cooking.dto.DishResponse;
import com.cooking.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<?> getAllDishes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        // 如果有关键词，使用搜索功能（不分页）
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<DishResponse> dishes = dishService.getAllDishes(keyword);
            return ResponseEntity.ok(dishes);
        }
        
        // 如果有分页参数，使用分页
        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size);
            Page<DishResponse> dishPage = dishService.getAllDishesWithPagination(category, pageable);
            return ResponseEntity.ok(dishPage);
        }
        
        // 否则返回全部列表
        List<DishResponse> dishes = dishService.getAllDishes(null);
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

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = dishService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<DishResponse>> getDishesByCategory(@PathVariable String category) {
        List<DishResponse> dishes = dishService.getDishesByCategory(category);
        return ResponseEntity.ok(dishes);
    }
}

