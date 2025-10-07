package com.lovai.lovaiapi.controller;

import com.lovai.lovaiapi.dto.memory.MemoryMediaCreateRequest;
import com.lovai.lovaiapi.dto.memory.MemoryMediaResponse;
import com.lovai.lovaiapi.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    @Autowired
    private MediaService mediaService;
  
    @PostMapping("/upload")
    public ResponseEntity<MemoryMediaResponse> uploadFileToMemory(
            @RequestParam("file") MultipartFile file,
            @RequestParam("memoryId") UUID memoryId,
            @RequestParam("folder") String folder) {
        
        try {
            MemoryMediaResponse response = mediaService.uploadFileToMemory(file, memoryId, folder);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/upload-multiple")
    public ResponseEntity<List<MemoryMediaResponse>> uploadMultipleFilesToMemory(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("memoryId") UUID memoryId,
            @RequestParam("folder") String folder) {
        
        try {
            List<MemoryMediaResponse> responses = mediaService.uploadMultipleFilesToMemory(files, memoryId, folder);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }    
    
    @GetMapping("/memory/{memoryId}")
    public ResponseEntity<List<MemoryMediaResponse>> getMemoryMedia(@PathVariable UUID memoryId) {
        try {
            List<MemoryMediaResponse> responses = mediaService.getMemoryMediaByMemoryId(memoryId);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/memory/{memoryId}/paginated")
    public ResponseEntity<Page<MemoryMediaResponse>> getMemoryMediaWithPagination(
            @PathVariable UUID memoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<MemoryMediaResponse> responses = mediaService.getMemoryMediaByMemoryIdWithPagination(memoryId, page, size);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/memory-media/{mediaId}")
    public ResponseEntity<MemoryMediaResponse> getMemoryMediaById(@PathVariable UUID mediaId) {
        try {
            MemoryMediaResponse response = mediaService.getMemoryMediaById(mediaId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/memory-media")
    public ResponseEntity<MemoryMediaResponse> createMemoryMedia(@RequestBody MemoryMediaCreateRequest request) {
        try {
            MemoryMediaResponse response = mediaService.createMemoryMediaFromUrl(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/memory-media/{mediaId}")
    public ResponseEntity<Map<String, Object>> deleteMemoryMedia(@PathVariable UUID mediaId) {
        try {
            mediaService.deleteMemoryMedia(mediaId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Memory media deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Delete failed: " + e.getMessage()
            ));
        }
    }
    
    @DeleteMapping("/memory/{memoryId}/all")
    public ResponseEntity<Map<String, Object>> deleteAllMemoryMedia(@PathVariable UUID memoryId) {
        try {
            mediaService.deleteAllMemoryMedia(memoryId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All memory media deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Delete failed: " + e.getMessage()
            ));
        }
    }
}
