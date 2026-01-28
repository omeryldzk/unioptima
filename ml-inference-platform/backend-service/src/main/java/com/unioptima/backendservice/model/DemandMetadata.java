package com.unioptima.backendservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "demand_metadata")
public class DemandMetadata {
    @Id
    private String id;
    private String type;
    private List<String> features;
    private String target;

    @Field("main_idOSYM")
    private List<String> mainIdOSYM;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<String> getMainIdOSYM() {
        return mainIdOSYM;
    }

    public void setMainIdOSYM(List<String> mainIdOSYM) {
        this.mainIdOSYM = mainIdOSYM;
    }
}
