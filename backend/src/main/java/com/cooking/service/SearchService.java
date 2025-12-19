package com.cooking.service;

import com.cooking.dto.DishResponse;
import com.cooking.dto.NoteResponse;
import com.cooking.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final DishService dishService;
    private final NoteService noteService;

    public SearchResponse searchAll(String keyword) {
        List<DishResponse> dishes = dishService.getAllDishes(keyword);
        List<NoteResponse> notes = noteService.searchPublicNotes(keyword);
        return new SearchResponse(dishes, notes);
    }
}

