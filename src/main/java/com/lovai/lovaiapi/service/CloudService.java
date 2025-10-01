package com.lovai.lovaiapi.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudService {
    
    String uploadFile(MultipartFile file, String folder);
    
    List<String> uploadMultipleFiles(List<MultipartFile> files, String folder);
    
    boolean deleteFile(String fileUrl);
    
    boolean deleteMultipleFiles(List<String> fileUrls);
    
    boolean fileExists(String fileUrl);
    
    FileInfo getFileInfo(String fileUrl);
    
    String getPublicUrl(String fileUrl);
    
    String getSignedUrl(String fileUrl, int expiresInSeconds);
    
    class FileInfo {
        private String name;
        private long size;
        private String contentType;
        private String lastModified;
        private String url;
        
        public FileInfo() {}
        
        public FileInfo(String name, long size, String contentType, String lastModified, String url) {
            this.name = name;
            this.size = size;
            this.contentType = contentType;
            this.lastModified = lastModified;
            this.url = url;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        
        public String getLastModified() { return lastModified; }
        public void setLastModified(String lastModified) { this.lastModified = lastModified; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}
