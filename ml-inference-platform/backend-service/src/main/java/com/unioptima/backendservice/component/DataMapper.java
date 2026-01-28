package com.unioptima.backendservice.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unioptima.backendservice.dto.RawDataDto;
import com.unioptima.backendservice.dto.SearchDataDto;
import com.unioptima.backendservice.model.RawData;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class DataMapper {

    private final ObjectMapper objectMapper;

    // Spring will automatically inject the configured ObjectMapper here
    public DataMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RawDataDto toDTO(RawData model) {
        if (model == null) return null;

        RawDataDto dto = new RawDataDto();
        dto.setId(model.getId());
        dto.setAcademicYear(model.getAcademicYear());
        dto.setIdOSYM(model.getIdOSYM());

        // Simply pass the map reference or copy it
        dto.setProperties(model.getExtraFields());

        return dto;
    }

    public RawData toEntity(RawDataDto dto) {
        RawData entity = new RawData();
        entity.setId(dto.getId());
        entity.setAcademicYear(dto.getAcademicYear());
        entity.setIdOSYM(dto.getIdOSYM());
        entity.setExtraFields(dto.getProperties());
        return entity;
    }

    public SearchDataDto toSearchDataDto(RawData entity){
        if(entity == null) return null;

        SearchDataDto dto = new SearchDataDto();
        dto.setId(entity.getId());
        dto.setUniversityName((String) entity.getExtraFields().get("universityName"));
        dto.setDepartmentName((String) entity.getExtraFields().get("departmentName"));
        dto.setLanguage((String) entity.getExtraFields().get("language"));
        dto.setFaculty((String) entity.getExtraFields().get("faculty"));
        dto.setScholarshipRate((String) entity.getExtraFields().get("scholarshipRate"));
        dto.setIdOSYM(entity.getIdOSYM());
        return dto;
    }

    public SearchDataDto toSearchDataDto(Document doc){
        if(doc == null) return null;

        SearchDataDto dto = new SearchDataDto();
        dto.setId(doc.get("_id").toString());
        dto.setUniversityName(doc.getString("universityName"));
        dto.setDepartmentName(doc.getString("departmentName"));
        dto.setLanguage(doc.getString("language"));
        dto.setFaculty(doc.getString("faculty"));
        dto.setIdOSYM(doc.getString("idOSYM"));
        // 2. Safe Numeric to String conversion
        // We get it as a generic Object first to avoid casting errors
        Object rateObj = doc.get("scholarshipRate");

        if (rateObj != null) {
            // valueOf handles Integers (50) and Doubles (50.5) safely
            dto.setScholarshipRate(String.valueOf(rateObj));
        } else {
            dto.setScholarshipRate("0"); // or null, depending on your UI needs
        }


        return dto;
    }

    // ==========================================
    // Exposing Jackson Functionalities
    // ==========================================

    /**
     * Generic method to map any object to another class using Jackson.
     * Usage: mapper.convert(myDto, MyEntity.class);
     */
    public <T> T convert(Object fromValue, Class<T> toValueType) {
        return objectMapper.convertValue(fromValue, toValueType);
    }

    /**
     * Helper to convert Object to JSON String
     */
    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting to JSON", e);
        }
    }

    /**
     * Helper to parse JSON String to Object
     */
    public <T> T fromJson(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON", e);
        }
    }

    /**
     * If you really need the raw ObjectMapper access occasionally
     */
    public ObjectMapper getMapper() {
        return this.objectMapper;
    }
}