package com.example.lovai.DTO.Upload;

import java.util.List;

public class UploadResponse {
    private boolean success;
    private String message;
    private String url;
    private List<String> urls;
    private String fileName;
    private long fileSize;
    private String contentType;

    public UploadResponse(){}
    public UploadResponse(boolean success, String message, String url, List<String> urls, String fileName, long fileSize, String contentType) {
        this.success = success;
        this.message = message;
        this.url = url;
        this.urls = urls;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getUrls() {
        return urls;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getContentType() {
        return contentType;
    }
}
