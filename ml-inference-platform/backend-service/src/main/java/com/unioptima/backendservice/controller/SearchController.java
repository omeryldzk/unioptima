package com.unioptima.backendservice.controller;

import com.unioptima.backendservice.dto.SearchRequest;
import com.unioptima.backendservice.dto.SearchResponseDto;
import com.unioptima.backendservice.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public ResponseEntity<SearchResponseDto> search(@RequestBody SearchRequest request) {
        try {
            SearchResponseDto response = searchService.search(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Handle typesense errors (e.g., connection issues)
            return ResponseEntity.internalServerError().build();
        }
    }

}
