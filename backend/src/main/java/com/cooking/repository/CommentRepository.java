package com.cooking.repository;

import com.cooking.entity.Comment;
import com.cooking.entity.Dish;
import com.cooking.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDishAndParentIsNullOrderByCreatedAtDesc(Dish dish);
    
    List<Comment> findByNoteAndParentIsNullOrderByCreatedAtDesc(Note note);
    
    List<Comment> findByParentOrderByCreatedAtAsc(Comment parent);
    
    void deleteByParent(Comment parent);
    
    void deleteByNote(Note note);
    
    List<Comment> findByNote(Note note);
}

