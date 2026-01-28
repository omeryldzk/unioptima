package com.unioptima.backendservice.dto;

import java.util.List;

public class SearchResponseDto {
    private long totalHits;
    private int page;
    private List<SearchDataDto> items;
    private List<FacetDto> facets; // For your UX dropdowns

    // Constructors
    public SearchResponseDto() {
    }
    public SearchResponseDto(long totalHits, int page, List<SearchDataDto> items, List<FacetDto> facets) {
        this.totalHits = totalHits;
        this.page = page;
        this.items = items;
        this.facets = facets;
    }

    // Getters and Setters
    public long getTotalHits() {
        return totalHits;
    }
    public void setTotalHits(long totalHits) {
        this.totalHits = totalHits;
    }
    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }
    public List<SearchDataDto> getItems() {
        return items;
    }
    public void setItems(List<SearchDataDto> items) {
        this.items = items;
    }
    public List<FacetDto> getFacets() {
        return facets;
    }
    public void setFacets(List<FacetDto> facets) {
        this.facets = facets;
    }
}
