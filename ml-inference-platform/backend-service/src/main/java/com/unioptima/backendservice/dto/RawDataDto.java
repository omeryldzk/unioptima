package com.unioptima.backendservice.dto;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.Map;

public class RawDataDto {
    private String id;
    private Integer academicYear;
    private String idOSYM;

    public RawDataDto() {
    }

    // We do not expose this map directly with a standard getter
    private Map<String, Object> properties = new HashMap<>();

    // Standard Getters and Setters for fixed fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Integer getAcademicYear() { return academicYear; }
    public void setAcademicYear(Integer academicYear) { this.academicYear = academicYear; }
    public String getIdOSYM() { return idOSYM; }
    public void setIdOSYM(String idOSYM) { this.idOSYM = idOSYM; }

    // When serializing to JSON, unwraps the map to root level
    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return properties;
    }

    // When receiving JSON, puts unknown fields into this map
    @JsonAnySetter
    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    // Helper for manual mapping
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}