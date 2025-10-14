package com.example.lovai.DTO.Memory;

public class MemoryMediaResponse {
    private String id;
    private String memoryId;
    private String url;
    private String mediaType;
    private String createdAt;

    public MemoryMediaResponse() {}
    public MemoryMediaResponse(String id, String memoryId, String url, String mediaType, String createdAt) {
        this.id = id;
        this.memoryId = memoryId;
        this.url = url;
        this.mediaType = mediaType;
        this.createdAt = createdAt;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getMemoryId() {
        return memoryId;
    }
    public void setMemoryId(String memoryId) {
        this.memoryId = memoryId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getMediaType() {
        return mediaType;
    }
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


}
