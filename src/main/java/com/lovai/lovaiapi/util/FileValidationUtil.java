package com.lovai.lovaiapi.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class FileValidationUtil {
    
    private static final List<String> SUPPORTED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    private static final List<String> SUPPORTED_VIDEO_TYPES = Arrays.asList(
        "video/mp4", "video/avi", "video/mov", "video/wmv", "video/webm"
    );
    
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB
    
    public static boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        return SUPPORTED_IMAGE_TYPES.contains(contentType) && 
               file.getSize() <= MAX_IMAGE_SIZE;
    }
    
    public static boolean isValidVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        return SUPPORTED_VIDEO_TYPES.contains(contentType) && 
               file.getSize() <= MAX_VIDEO_SIZE;
    }
    
    public static boolean isValidMediaFile(MultipartFile file) {
        return isValidImage(file) || isValidVideo(file);
    }
    
    public static String getFileType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "unknown";
        }
        
        String contentType = file.getContentType();
        if (SUPPORTED_IMAGE_TYPES.contains(contentType)) {
            return "image";
        } else if (SUPPORTED_VIDEO_TYPES.contains(contentType)) {
            return "video";
        } else {
            return "unknown";
        }
    }
    
    public static String getFileSizeString(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        
        return "";
    }
}
