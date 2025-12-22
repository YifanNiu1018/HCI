package com.cooking.repository;

import com.cooking.entity.History;
import com.cooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    @Query("SELECT h FROM History h WHERE h.user = :user ORDER BY h.viewedAt DESC")
    List<History> findByUserOrderByViewedAtDesc(@Param("user") User user);

    @Query("SELECT h FROM History h WHERE h.user = :user AND h.dish IS NOT NULL ORDER BY h.viewedAt DESC")
    List<History> findDishHistoriesByUserOrderByViewedAtDesc(@Param("user") User user);

    @Query("SELECT h FROM History h WHERE h.user = :user AND h.note IS NOT NULL ORDER BY h.viewedAt DESC")
    List<History> findNoteHistoriesByUserOrderByViewedAtDesc(@Param("user") User user);

    @Query("SELECT h FROM History h WHERE h.user = :user AND h.dish.id = :dishId ORDER BY h.viewedAt DESC")
    List<History> findByUserAndDishId(@Param("user") User user, @Param("dishId") Long dishId);

    @Query("SELECT h FROM History h WHERE h.user = :user AND h.note.id = :noteId ORDER BY h.viewedAt DESC")
    List<History> findByUserAndNoteId(@Param("user") User user, @Param("noteId") Long noteId);
    
    void deleteByUser(User user);
}

