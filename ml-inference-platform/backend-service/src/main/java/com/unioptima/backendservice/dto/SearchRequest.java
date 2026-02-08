package com.unioptima.backendservice.dto;

import java.util.Map;

public class SearchRequest {
    String q; // The user's current keystrokes
    String targetField; // Which field are they currently typing in? (e.g., "faculty")
    Map<String, String> locks; // Previous selections: {"uni": "Oxford"}

    public SearchRequest() {
    }

    public SearchRequest(String q, String targetField, Map<String, String> locks) {
        this.q = q;
        this.targetField = targetField;
        this.locks = locks;
    }

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public Map<String, String> getLocks() {
        return locks;
    }

    public void setLocks(Map<String, String> locks) {
        this.locks = locks;
    }
}
