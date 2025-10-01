package com.lovai.lovaiapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovai.lovaiapi.exception.CloudStorageException;
import com.lovai.lovaiapi.util.FileValidationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SupabaseCloudService implements CloudService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucketName;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SupabaseCloudService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new CloudStorageException("File không được để trống");
        }
        
        if (!FileValidationUtil.isValidMediaFile(file)) {
            throw new CloudStorageException("File không được hỗ trợ. Chỉ chấp nhận ảnh và video");
        }
        
        try {
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String filePath = folder + "/" + fileName;
            
            HttpHeaders headers = createHeaders();
            HttpEntity<byte[]> request = new HttpEntity<>(file.getBytes(), headers);
            
            String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath;
            
            ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl, 
                HttpMethod.POST, 
                request, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + filePath;
            } else {
                throw new CloudStorageException("Không thể upload file: " + response.getBody());
            }
            
        } catch (IOException e) {
            throw new CloudStorageException("Lỗi đọc file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CloudStorageException("Lỗi upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> uploadMultipleFiles(List<MultipartFile> files, String folder) {
        return files.stream()
                .map(file -> uploadFile(file, folder))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            String filePath = extractFilePathFromUrl(fileUrl);
            HttpHeaders headers = createHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath;
            
            ResponseEntity<String> response = restTemplate.exchange(
                deleteUrl, 
                HttpMethod.DELETE, 
                request, 
                String.class
            );
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deleteMultipleFiles(List<String> fileUrls) {
        return fileUrls.stream()
                .allMatch(this::deleteFile);
    }

    @Override
    public boolean fileExists(String fileUrl) {
        try {
            String filePath = extractFilePathFromUrl(fileUrl);
            HttpHeaders headers = createHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            String checkUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath;
            
            ResponseEntity<String> response = restTemplate.exchange(
                checkUrl, 
                HttpMethod.HEAD, 
                request, 
                String.class
            );
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public FileInfo getFileInfo(String fileUrl) {
        try {
            String filePath = extractFilePathFromUrl(fileUrl);
            HttpHeaders headers = createHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            String infoUrl = supabaseUrl + "/storage/v1/object/info/" + bucketName + "/" + filePath;
            
            ResponseEntity<String> response = restTemplate.exchange(
                infoUrl, 
                HttpMethod.GET, 
                request, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                FileInfo fileInfo = new FileInfo();
                fileInfo.setName(jsonNode.get("name").asText());
                fileInfo.setSize(jsonNode.get("size").asLong());
                fileInfo.setContentType(jsonNode.get("mimetype").asText());
                fileInfo.setLastModified(jsonNode.get("updated_at").asText());
                fileInfo.setUrl(fileUrl);
                
                return fileInfo;
            }
            
        } catch (Exception e) {
            
        }
        
        return null;
    }

    @Override
    public String getPublicUrl(String fileUrl) {
        return fileUrl;
    }

    @Override
    public String getSignedUrl(String fileUrl, int expiresInSeconds) {
        try {
            String filePath = extractFilePathFromUrl(fileUrl);
            HttpHeaders headers = createHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            String signedUrlEndpoint = supabaseUrl + "/storage/v1/object/sign/" + bucketName + "/" + filePath;
            String signedUrl = signedUrlEndpoint + "?expiresIn=" + expiresInSeconds;
            
            ResponseEntity<String> response = restTemplate.exchange(
                signedUrl, 
                HttpMethod.POST, 
                request, 
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return jsonNode.get("signedURL").asText();
            }
            
        } catch (Exception e) {
        }
        
        return fileUrl;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);
        return headers;
    }

    private String generateUniqueFileName(String originalFilename) {
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        if (originalFilename != null && !originalFilename.isEmpty()) {
            String extension = "";
            int lastDotIndex = originalFilename.lastIndexOf('.');
            if (lastDotIndex > 0) {
                extension = originalFilename.substring(lastDotIndex);
            }
            return timestamp + "_" + uuid + extension;
        }
        
        return timestamp + "_" + uuid;
    }

    private String extractFilePathFromUrl(String fileUrl) {
        
        String[] parts = fileUrl.split("/storage/v1/object/public/" + bucketName + "/");
        if (parts.length > 1) {
            return parts[1];
        }
        
        String[] objectParts = fileUrl.split("/storage/v1/object/" + bucketName + "/");
        if (objectParts.length > 1) {
            return objectParts[1];
        }
        
        throw new IllegalArgumentException("Invalid file URL format: " + fileUrl);
    }
}
