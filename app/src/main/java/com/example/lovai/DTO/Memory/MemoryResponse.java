package com.example.lovai.DTO.Memory;

public class MemoryResponse {
    private String id;
    private String coupleId;
    private String title;
    private String description;
    private String happenedAt;
    private String locationText;
    private int mediaCount;
    private String createdAt;

    public MemoryResponse() {
    }

    public MemoryResponse(String id, String coupleId, String title, String description, String happenedAt, String locationText, int mediaCount, String createdAt) {
        this.id = id;
        this.coupleId = coupleId;
        this.title = title;
        this.description = description;
        this.happenedAt = happenedAt;
        this.locationText = locationText;
        this.mediaCount = mediaCount;
        this.createdAt = createdAt;
    }

    // Getter cho id
    public String getId() {
        return id;
    }

    // Setter cho id
    public void setId(String id) {
        this.id = id;
    }

    // Getter cho coupleId
    public String getCoupleId() {
        return coupleId;
    }

    // Setter cho coupleId
    public void setCoupleId(String coupleId) {
        this.coupleId = coupleId;
    }

    // Getter cho title
    public String getTitle() {
        return title;
    }

    // Setter cho title
    public void setTitle(String title) {
        this.title = title;
    }

    // Getter cho description
    public String getDescription() {
        return description;
    }

    // Setter cho description
    public void setDescription(String description) {
        this.description = description;
    }

    // Getter cho happenedAt
    public String getHappenedAt() {
        return happenedAt;
    }

    // Setter cho happenedAt
    public void setHappenedAt(String happenedAt) {
        this.happenedAt = happenedAt;
    }

    // Getter cho locationText
    public String getLocationText() {
        return locationText;
    }

    // Setter cho locationText
    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    // Getter cho mediaCount
    public int getMediaCount() {
        return mediaCount;
    }

    // Setter cho mediaCount
    public void setMediaCount(int mediaCount) {
        this.mediaCount = mediaCount;
    }

    // Getter cho createdAt
    public String getCreatedAt() {
        return createdAt;
    }

    // Setter cho createdAt
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
