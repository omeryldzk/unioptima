package com.unioptima.backendservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "encoded_baseRanking_data")
public class EncodedBaseRankingData {

    @Id
    private String id;

    private Integer academicYear;
    private String idOSYM;

    // Catch-all for other fields
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
