package com.example.lovai.DTO.Memory;

public class MemoryMediaCreateRequest {
    private String memoryId;
    private String url;
    private String mediaType;
    private Integer width;
    private Integer height;

    public MemoryMediaCreateRequest(){}
    public MemoryMediaCreateRequest(String memoryId, String url, String mediaType, Integer width, Integer height) {
        this.memoryId = memoryId;
        this.url = url;
        this.mediaType = mediaType;
        this.width = width;
        this.height = height;
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

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

}
