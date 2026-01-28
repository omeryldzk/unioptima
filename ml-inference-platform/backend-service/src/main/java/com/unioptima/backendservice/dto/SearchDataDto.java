package com.unioptima.backendservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchDataDto {

    // This matches the Typesense required field "id"
    @JsonProperty("id")
    private String id;

    @JsonProperty("universityName")
    private String universityName;

    @JsonProperty("departmentName")
    private String departmentName;

    @JsonProperty("language")
    private String language;

    @JsonProperty("faculty")
    private String faculty;

    @JsonProperty("scholarshipRate")
    private String scholarshipRate;

    @JsonProperty("idOSYM")
    private String idOSYM;

    // --- Constructors ---

    // Required for Jackson Deserialization
    public SearchDataDto() {
    }

    // UPDATED: Added 'id' to the constructor to ensure it's not forgotten
    public SearchDataDto(String id, String universityName, String departmentName, String language, String faculty, String scholarshipRate, String idOSYM) {
        this.id = id;
        this.universityName = universityName;
        this.departmentName = departmentName;
        this.language = language;
        this.faculty = faculty;
        this.scholarshipRate = scholarshipRate;
        this.idOSYM = idOSYM;
    }

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getScholarshipRate() {
        return scholarshipRate;
    }

    public void setScholarshipRate(String scholarshipRate) {
        this.scholarshipRate = scholarshipRate;
    }

    public String getIdOSYM() {
        return idOSYM;
    }
    public void setIdOSYM(String idOSYM) {
        this.idOSYM = idOSYM;
    }
}