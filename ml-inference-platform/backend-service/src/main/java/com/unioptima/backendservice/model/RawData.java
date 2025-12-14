package com.unioptima.backendservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "raw_data")
public class RawData {

    @Id
    private String id;

    // Raw Data academicYear in previous file was String, checking compatibility.
    // Prompt said "private Integer academicYear;". I will use Integer as requested.
    private Integer academicYear;
    private String idOSYM;

    private Map<String, Object> extraFields = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(Integer academicYear) {
        this.academicYear = academicYear;
    }

    public String getIdOSYM() {
        return idOSYM;
    }

    public void setIdOSYM(String idOSYM) {
        this.idOSYM = idOSYM;
    }

    public Map<String, Object> getExtraFields() {
        return extraFields;
    }

    public void setExtraFields(Map<String, Object> extraFields) {
        this.extraFields = extraFields;
    }
}
