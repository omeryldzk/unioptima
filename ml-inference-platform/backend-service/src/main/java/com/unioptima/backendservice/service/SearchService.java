package com.unioptima.backendservice.service;

import com.unioptima.backendservice.dto.SearchRequest;
import com.unioptima.backendservice.dto.SearchResponseDto;
import org.typesense.model.SearchResult;

public interface SearchService {
    SearchResponseDto search(SearchRequest req) throws Exception;
}
