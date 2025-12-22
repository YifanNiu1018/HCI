package com.cooking.service;

import com.cooking.dto.DishResponse;
import com.cooking.entity.Dish;
import com.cooking.entity.User;
import com.cooking.repository.DishRepository;
import com.cooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishService {

    private final DishRepository dishRepository;
    private final UserRepository userRepository;
    private final com.cooking.service.HistoryService historyService;

    public List<DishResponse> getAllDishes(String keyword) {
        List<Dish> dishes;
        if (keyword != null && !keyword.trim().isEmpty()) {
            dishes = dishRepository.searchByKeyword(keyword.trim());
        } else {
            dishes = dishRepository.findAll();
        }

        String username = getCurrentUsername();
        return dishes.stream()
                .map(dish -> {
                    boolean isFavorite = username != null && isFavorite(dish.getId(), username);
                    return DishResponse.from(dish, isFavorite);
                })
                .collect(Collectors.toList());
    }

    public Page<DishResponse> getAllDishesWithPagination(String category, Pageable pageable) {
        Page<Dish> dishPage;
        if (category != null && !category.trim().isEmpty() && !category.equals("all")) {
            dishPage = dishRepository.findByCategory(category, pageable);
        } else {
            dishPage = dishRepository.findAll(pageable);
        }

        String username = getCurrentUsername();
        return dishPage.map(dish -> {
            boolean isFavorite = username != null && isFavorite(dish.getId(), username);
            return DishResponse.from(dish, isFavorite);
        });
    }

    public DishResponse getDishById(Long id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("菜品不存在"));

        String username = getCurrentUsername();
        boolean isFavorite = username != null && isFavorite(id, username);
        
        // 记录浏览历史
        if (username != null) {
            historyService.recordDishView(id);
        }
        
        return DishResponse.from(dish, isFavorite);
    }

    @Transactional
    public DishResponse toggleFavorite(Long dishId) {
        String username = getCurrentUsername();
        if (username == null) {
            throw new RuntimeException("请先登录");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("菜品不存在"));

        boolean isFavorite;
        if (user.getFavoriteDishes().contains(dish)) {
            user.getFavoriteDishes().remove(dish);
            isFavorite = false;
        } else {
            user.getFavoriteDishes().add(dish);
            isFavorite = true;
        }

        userRepository.save(user);
        return DishResponse.from(dish, isFavorite);
    }

    public List<DishResponse> getFavorites() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new RuntimeException("请先登录");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return user.getFavoriteDishes().stream()
                .map(dish -> DishResponse.from(dish, true))
                .collect(Collectors.toList());
    }

    public List<DishResponse> getDishesByCategory(String category) {
        List<Dish> dishes = dishRepository.findByCategory(category);
        String username = getCurrentUsername();
        return dishes.stream()
                .map(dish -> {
                    boolean isFavorite = username != null && isFavorite(dish.getId(), username);
                    return DishResponse.from(dish, isFavorite);
                })
                .collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        return dishRepository.findAllDistinctCategories();
    }

    private boolean isFavorite(Long dishId, String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;
        return user.getFavoriteDishes().stream()
                .anyMatch(dish -> dish.getId().equals(dishId));
    }

    private String getCurrentUsername() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return null;
        }
    }
}

