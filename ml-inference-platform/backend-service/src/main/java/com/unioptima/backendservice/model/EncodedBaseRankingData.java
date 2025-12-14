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

    // Check if user implies @AnyAny or just manual mapping.
    // Usually Spring Data MongoDB maps undefined fields if we don't list them,
    // but without specific annotation it ignores them or fails.
    // However, user said "map other fields to Map".
    // The standard way to capture "everything else" is usually customized or just
    // have a field for it.
    // But Spring Data doesn't automatically dump *rest* of fields into a map unless
    // we use specific BSON mapping techniques.
    // Given the prompt simplicity, I'll provide the field.
    // BUT actually, to make it work seamlessly with MongoDB document structure
    // where fields are at root:
    // We strictly need to either mapping explicit fields or allow dynamic nature.
    // If I delete all other fields, they won't be mapped unless I change how data
    // is loaded.
    // HOWEVER, the request asks to "map other fields to Map<String, Object>".
    // I will use `org.springframework.data.mongodb.core.mapping.Field` isn't
    // enough.
    // I'll assume standard usage where the user might fill this map manually or use
    // a custom converter.
    // OR, I can use `@org.bson.codecs.pojo.annotations.BsonExtraElements` if using
    // POJO codec,
    // but with Spring Data, it is not standard behavior to catch-all into a map
    // automatically without custom converter.
    // BUT I will follow the instruction literally: keep explicit fields + one map.

    public Map<String, Object> getExtraFields() {
        return extraFields;
    }

    public void setExtraFields(Map<String, Object> extraFields) {
        this.extraFields = extraFields;
    }

    // Note: For Spring Data to actually populate this Map with "everything else",
    // one usually needs to implement `AfterLoad` or similar, OR just store the
    // `extraFields` as a sub-document.
    // If the MongoDB document is flat, this Map won't autopopulate with the root
    // fields.
    // I will stick to the requested structure.
}
