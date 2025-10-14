package com.example.lovai.DTO.Memory;

import java.util.Map;

public class MemoryCreateRequest {
    private String coupleId;
    private String title;

    private String description;
    private String happenedAt;
    private String locationText;
    private Map<String, Object> meta;
    public MemoryCreateRequest() {}

    public MemoryCreateRequest(String coupleId, String title, String description, String happenedAt) {
        this.coupleId = coupleId;
        this.title = title;
        this.description = description;
        this.happenedAt = happenedAt;
    }
    public String getCoupleId() {
        return coupleId;
    }

    public void setCoupleId(String coupleId) {
        this.coupleId = coupleId;
    }

    public  String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }


    public String getHappenedAt() {
        return happenedAt;
    }
    public void setHappenedAt(String happenedAt) {
        this.happenedAt = happenedAt;
    }


    public String getLocationText() {
        return locationText;
    }
    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }





}
