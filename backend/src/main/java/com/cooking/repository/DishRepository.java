package com.cooking.repository;

import com.cooking.entity.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    @Query("SELECT d FROM Dish d WHERE " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "EXISTS (SELECT i FROM d.ingredients i WHERE LOWER(i) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Dish> searchByKeyword(@Param("keyword") String keyword);
}

