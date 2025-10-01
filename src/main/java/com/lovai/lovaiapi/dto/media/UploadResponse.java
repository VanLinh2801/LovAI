package com.lovai.lovaiapi.dto.media;

import java.util.List;

public class UploadResponse {
    
    private boolean success;
    private String message;
    private String url;
    private List<String> urls;
    private String fileName;
    private long fileSize;
    private String contentType;
    
    public UploadResponse() {}
    
    public UploadResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public UploadResponse(boolean success, String message, String url) {
        this.success = success;
        this.message = message;
        this.url = url;
    }
    
    public UploadResponse(boolean success, String message, List<String> urls) {
        this.success = success;
        this.message = message;
        this.urls = urls;
    }
    
    public static UploadResponse success(String url) {
        return new UploadResponse(true, "Upload thành công", url);
    }
    
    public static UploadResponse success(List<String> urls) {
        return new UploadResponse(true, "Upload thành công", urls);
    }
    
    public static UploadResponse error(String message) {
        return new UploadResponse(false, message);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public List<String> getUrls() {
        return urls;
    }
    
    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
