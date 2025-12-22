package com.cooking.repository;

import com.cooking.entity.Note;
import com.cooking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUser(User user);
    
    List<Note> findByIsPublicTrue();
    
    @Query("SELECT n FROM Note n WHERE n.isPublic = true AND " +
           "(LOWER(n.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "EXISTS (SELECT i FROM n.ingredients i WHERE LOWER(i) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
           "EXISTS (SELECT t FROM n.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    List<Note> searchPublicNotes(@Param("keyword") String keyword);
    
    @Query("SELECT n FROM Note n WHERE n.user = :user AND " +
           "(LOWER(n.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "EXISTS (SELECT i FROM n.ingredients i WHERE LOWER(i) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    List<Note> searchUserNotes(@Param("user") User user, @Param("keyword") String keyword);
}

