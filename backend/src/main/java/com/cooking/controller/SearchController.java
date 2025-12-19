package com.cooking.controller;

import com.cooking.dto.SearchResponse;
import com.cooking.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponse> searchAll(@RequestParam(required = false) String keyword) {
        SearchResponse response = searchService.searchAll(keyword);
        return ResponseEntity.ok(response);
    }
}

