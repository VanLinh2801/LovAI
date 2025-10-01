package com.lovai.lovaiapi.service;

import com.lovai.lovaiapi.dto.memory.MemoryMediaCreateRequest;
import com.lovai.lovaiapi.dto.memory.MemoryMediaResponse;
import com.lovai.lovaiapi.exception.NotFoundException;
import com.lovai.lovaiapi.model.Memory;
import com.lovai.lovaiapi.model.MemoryMedia;
import com.lovai.lovaiapi.repository.MemoryMediaRepository;
import com.lovai.lovaiapi.repository.MemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MediaService {
    
    @Autowired
    private CloudService cloudService;
    
    @Autowired
    private MemoryRepository memoryRepository;
    
    @Autowired
    private MemoryMediaRepository memoryMediaRepository;
    
    public MemoryMediaResponse uploadFileToMemory(MultipartFile file, UUID memoryId, String folder) {
        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new NotFoundException("Memory not found"));
        
        String url = cloudService.uploadFile(file, folder);
        
        String mediaType = determineMediaType(file.getContentType());
        
        MemoryMedia memoryMedia = MemoryMedia.builder()
                .memory(memory)
                .url(url)
                .mediaType(mediaType)
                .width(null) // Có thể extract từ EXIF sau này
                .height(null) // Có thể extract từ EXIF sau này
                .exifJson(null) // Có thể extract EXIF data sau này
                .build();
        
        MemoryMedia savedMedia = memoryMediaRepository.save(memoryMedia);
        
        updateMemoryMediaCount(memoryId);
        
        return convertToResponse(savedMedia);
    }
    
    public List<MemoryMediaResponse> uploadMultipleFilesToMemory(List<MultipartFile> files, UUID memoryId, String folder) {
        memoryRepository.findById(memoryId)
                .orElseThrow(() -> new NotFoundException("Memory not found"));
        
        return files.stream()
                .map(file -> {
                    try {
                        return uploadFileToMemory(file, memoryId, folder);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename(), e);
                    }
                })
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<MemoryMediaResponse> getMemoryMediaByMemoryId(UUID memoryId) {
        List<MemoryMedia> mediaList = memoryMediaRepository.findByMemoryId(memoryId);
        return mediaList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<MemoryMediaResponse> getMemoryMediaByMemoryIdWithPagination(UUID memoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<MemoryMedia> mediaList = memoryMediaRepository.findByMemoryIdWithPagination(memoryId, pageable);
        
        List<MemoryMediaResponse> responses = mediaList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        long total = memoryMediaRepository.countByMemoryId(memoryId);
        return new PageImpl<>(responses, pageable, total);
    }
    
    @Transactional(readOnly = true)
    public MemoryMediaResponse getMemoryMediaById(UUID mediaId) {
        MemoryMedia media = memoryMediaRepository.findById(mediaId)
                .orElseThrow(() -> new NotFoundException("Memory media not found"));
        return convertToResponse(media);
    }
    
    public void deleteMemoryMedia(UUID mediaId) {
        MemoryMedia media = memoryMediaRepository.findById(mediaId)
                .orElseThrow(() -> new NotFoundException("Memory media not found"));
        
        try {
            cloudService.deleteFile(media.getUrl());
        } catch (Exception e) {
            System.err.println("Failed to delete file from cloud storage: " + e.getMessage());
        }
        
        memoryMediaRepository.delete(media);
        
        updateMemoryMediaCount(media.getMemory().getId());
    }
    
    public void deleteAllMemoryMedia(UUID memoryId) {
        List<MemoryMedia> mediaList = memoryMediaRepository.findByMemoryId(memoryId);
        
        for (MemoryMedia media : mediaList) {
            try {
                cloudService.deleteFile(media.getUrl());
            } catch (Exception e) {
                System.err.println("Failed to delete file from cloud storage: " + e.getMessage());
            }
        }
        
        memoryMediaRepository.deleteByMemoryId(memoryId);
        
        updateMemoryMediaCount(memoryId);
    }
    
    public MemoryMediaResponse createMemoryMediaFromUrl(MemoryMediaCreateRequest request) {
        Memory memory = memoryRepository.findById(request.getMemoryId())
                .orElseThrow(() -> new NotFoundException("Memory not found"));
        
        MemoryMedia memoryMedia = MemoryMedia.builder()
                .memory(memory)
                .url(request.getUrl())
                .mediaType(request.getMediaType())
                .width(request.getWidth())
                .height(request.getHeight())
                .exifJson(request.getExifJson())
                .build();
        
        MemoryMedia savedMedia = memoryMediaRepository.save(memoryMedia);
        
        updateMemoryMediaCount(request.getMemoryId());
        
        return convertToResponse(savedMedia);
    }
    
    private void updateMemoryMediaCount(UUID memoryId) {
        long mediaCount = memoryMediaRepository.countByMemoryId(memoryId);
        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new NotFoundException("Memory not found"));
        memory.setMediaCount((int) mediaCount);
        memoryRepository.save(memory);
    }
    
    private String determineMediaType(String contentType) {
        if (contentType == null) {
            return "file";
        }
        
        if (contentType.startsWith("image/")) {
            return "image";
        } else if (contentType.startsWith("video/")) {
            return "video";
        } else if (contentType.startsWith("audio/")) {
            return "audio";
        } else {
            return "file";
        }
    }
    
    private MemoryMediaResponse convertToResponse(MemoryMedia memoryMedia) {
        return MemoryMediaResponse.builder()
                .id(memoryMedia.getId())
                .memoryId(memoryMedia.getMemory().getId())
                .url(memoryMedia.getUrl())
                .mediaType(memoryMedia.getMediaType())
                .width(memoryMedia.getWidth())
                .height(memoryMedia.getHeight())
                .exifJson(memoryMedia.getExifJson())
                .createdAt(memoryMedia.getCreatedAt())
                .build();
    }
}
