package com.unioptima.backendservice.dto;

public class FacetDto {
    private String value; // The faculty name or uni name
    private Integer count; // How many documents matched

    public FacetDto(String value, Integer count) {
        this.value = value;
        this.count = count;
    }
    // Getters and Setters
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public Integer getCount() {
        return count;
    }
    public void setCount(Integer count) {
        this.count = count;
    }
}