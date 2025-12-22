package com.cooking.repository;

import com.cooking.entity.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    @Query("SELECT d FROM Dish d WHERE " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "EXISTS (SELECT i FROM d.ingredients i WHERE LOWER(i) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Dish> searchByKeyword(@Param("keyword") String keyword);
    
    List<Dish> findByCategory(String category);
    
    Page<Dish> findByCategory(String category, Pageable pageable);
    
    @Query("SELECT DISTINCT d.category FROM Dish d WHERE d.category IS NOT NULL ORDER BY d.category")
    List<String> findAllDistinctCategories();
}

