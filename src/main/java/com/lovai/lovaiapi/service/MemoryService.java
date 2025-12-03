package com.lovai.lovaiapi.service;

import com.lovai.lovaiapi.dto.memory.MemoryCreateRequest;
import com.lovai.lovaiapi.dto.memory.MemoryDeleteManyRequest;
import com.lovai.lovaiapi.dto.memory.MemoryResponse;
import com.lovai.lovaiapi.dto.memory.MemoryStatisticsResponse;
import com.lovai.lovaiapi.dto.memory.MemoryUpdateRequest;
import com.lovai.lovaiapi.exception.NotFoundException;
import com.lovai.lovaiapi.model.Couple;
import com.lovai.lovaiapi.model.Memory;
import com.lovai.lovaiapi.repository.CoupleRepository;
import com.lovai.lovaiapi.repository.MemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MemoryService {
    
    @Autowired
    private MemoryRepository memoryRepository;
    
    @Autowired
    private CoupleRepository coupleRepository;
    
    public MemoryResponse createMemory(MemoryCreateRequest request) {
        Couple couple = coupleRepository.findById(request.getCoupleId())
                .orElseThrow(() -> new NotFoundException("Couple not found"));
        
        Memory memory = Memory.builder()
                .couple(couple)
                .title(request.getTitle())
                .description(request.getDescription())
                .happenedAt(request.getHappenedAt() != null ? request.getHappenedAt().atStartOfDay(ZoneOffset.UTC).toOffsetDateTime() : OffsetDateTime.now())
                .locationText(request.getLocationText())
                .mediaCount(0)
                .meta(request.getMeta() != null ? request.getMeta() : Map.of())
                .build();
        
        Memory savedMemory = memoryRepository.save(memory);
        return convertToResponse(savedMemory);
    }
    
    @Transactional(readOnly = true)
    public MemoryResponse getMemoryById(UUID memoryId, UUID coupleId) {
        Memory memory = memoryRepository.findByIdAndCoupleId(memoryId, coupleId);
        if (memory == null) {
            throw new NotFoundException("Memory not found");
        }
        return convertToResponse(memory);
    }
    
    @Transactional(readOnly = true)
    public List<MemoryResponse> getMemoriesByCoupleId(UUID coupleId) {
        List<Memory> memories = memoryRepository.findByCoupleIdAndNotDeleted(coupleId);
        return memories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<MemoryResponse> getMemoriesByCoupleIdWithPagination(UUID coupleId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Memory> memories = memoryRepository.findByCoupleIdWithPagination(coupleId, pageable);
        
        List<MemoryResponse> responses = memories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        long total = memoryRepository.countByCoupleId(coupleId);
        return new PageImpl<>(responses, pageable, total);
    }
    
    @Transactional(readOnly = true)
    public List<MemoryResponse> searchMemories(UUID coupleId, String searchText) {
        List<Memory> memories = memoryRepository.findByCoupleIdAndSearchText(coupleId, searchText);
        return memories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<MemoryResponse> searchMemoriesWithPagination(UUID coupleId, String searchText, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Memory> memories = memoryRepository.findByCoupleIdAndSearchTextWithPagination(coupleId, searchText, pageable);
        
        List<MemoryResponse> responses = memories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        long total = memoryRepository.findByCoupleIdAndSearchText(coupleId, searchText).size();
        return new PageImpl<>(responses, pageable, total);
    }
    
    @Transactional(readOnly = true)
    public List<MemoryResponse> getMemoriesByDateRange(UUID coupleId, LocalDate startDate, LocalDate endDate) {
        OffsetDateTime startDateTime = startDate.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime endDateTime = endDate.atTime(23, 59, 59, 999999999).atOffset(ZoneOffset.UTC);
        List<Memory> memories = memoryRepository.findByCoupleIdAndDateRange(coupleId, startDateTime, endDateTime);
        return memories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public MemoryResponse updateMemory(UUID memoryId, UUID coupleId, MemoryUpdateRequest request) {
        Memory memory = memoryRepository.findByIdAndCoupleId(memoryId, coupleId);
        if (memory == null) {
            throw new NotFoundException("Memory not found");
        }
        
        if (request.getTitle() != null) {
            memory.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            memory.setDescription(request.getDescription());
        }
        if (request.getHappenedAt() != null) {
            memory.setHappenedAt(request.getHappenedAt().atStartOfDay(ZoneOffset.UTC).toOffsetDateTime());
        }
        if (request.getLocationText() != null) {
            memory.setLocationText(request.getLocationText());
        }
        if (request.getMeta() != null) {
            memory.setMeta(request.getMeta());
        }
        
        Memory updatedMemory = memoryRepository.save(memory);
        return convertToResponse(updatedMemory);
    }
    
    public void deleteMemory(UUID memoryId, UUID coupleId) {
        Memory memory = memoryRepository.findByIdAndCoupleId(memoryId, coupleId);
        if (memory == null) {
            throw new NotFoundException("Memory not found");
        }
        
        memory.setDeletedAt(OffsetDateTime.now());
        memoryRepository.save(memory);
    }
    
    public Map<String, Object> deleteManyMemories(MemoryDeleteManyRequest request) {
        int deletedCount = 0;
        int notFoundCount = 0;
        
        for (UUID memoryId : request.getMemoryIds()) {
            Memory memory = memoryRepository.findByIdAndCoupleId(memoryId, request.getCoupleId());
            if (memory != null) {
                memory.setDeletedAt(OffsetDateTime.now());
                memoryRepository.save(memory);
                deletedCount++;
            } else {
                notFoundCount++;
            }
        }
        
        return Map.of(
            "success", true,
            "deletedCount", deletedCount,
            "notFoundCount", notFoundCount,
            "totalRequested", request.getMemoryIds().size()
        );
    }
    
    @Transactional(readOnly = true)
    public long countMemoriesByCoupleId(UUID coupleId) {
        return memoryRepository.countByCoupleId(coupleId);
    }
    
    @Transactional(readOnly = true)
    public MemoryStatisticsResponse getMemoryStatistics(UUID coupleId, LocalDate startDate, LocalDate endDate, String locationText) {
        long totalMemories = memoryRepository.countByCoupleId(coupleId);
        long memoriesInDateRange = 0;
        long memoriesByLocation = 0;
        
        Map<String, Object> dateRangeFilter = Map.of();
        Map<String, Object> locationFilter = Map.of();
        
        if (startDate != null && endDate != null) {
            OffsetDateTime startDateTime = startDate.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
            OffsetDateTime endDateTime = endDate.atTime(23, 59, 59, 999999999).atOffset(ZoneOffset.UTC);
            memoriesInDateRange = memoryRepository.countByCoupleIdAndDateRange(coupleId, startDateTime, endDateTime);
            dateRangeFilter = Map.of(
                "startDate", startDate,
                "endDate", endDate
            );
        }
        
        if (locationText != null && !locationText.trim().isEmpty()) {
            memoriesByLocation = memoryRepository.countByCoupleIdAndLocation(coupleId, locationText);
            locationFilter = Map.of("locationText", locationText);
        }
        
        Map<String, Long> statistics = Map.of(
            "totalMemories", totalMemories,
            "memoriesInDateRange", memoriesInDateRange,
            "memoriesByLocation", memoriesByLocation
        );
        
        return MemoryStatisticsResponse.builder()
                .coupleId(coupleId)
                .totalMemories(totalMemories)
                .memoriesInDateRange(memoriesInDateRange)
                .memoriesByLocation(memoriesByLocation)
                .dateRangeFilter(dateRangeFilter)
                .locationFilter(locationFilter)
                .statistics(statistics)
                .build();
    }
    
    @Transactional(readOnly = true)
    public long countMemoriesInLastMonth(UUID coupleId) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate oneMonthAgo = today.minusMonths(1);
        OffsetDateTime startDateTime = oneMonthAgo.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime endDateTime = today.atTime(23, 59, 59, 999_999_999).atOffset(ZoneOffset.UTC);
        return memoryRepository.countByCoupleIdAndDateRange(coupleId, startDateTime, endDateTime);
    }
    
    @Transactional(readOnly = true)
    public long countMemoryMediaByCoupleId(UUID coupleId) {
        return memoryRepository.sumMediaCountByCoupleId(coupleId);
    }
    
    @Transactional(readOnly = true)
    public long countMemoryMediaInDateRange(UUID coupleId, LocalDate startDate, LocalDate endDate) {
        OffsetDateTime startDateTime = startDate.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime endDateTime = endDate.atTime(23, 59, 59, 999_999_999).atOffset(ZoneOffset.UTC);
        return memoryRepository.sumMediaCountByCoupleIdAndDateRange(coupleId, startDateTime, endDateTime);
    }
    
    public void updateMediaCount(UUID memoryId, int mediaCount) {
        Memory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new NotFoundException("Memory not found"));
        memory.setMediaCount(mediaCount);
        memoryRepository.save(memory);
    }
    
    private MemoryResponse convertToResponse(Memory memory) {
        return MemoryResponse.builder()
                .id(memory.getId())
                .coupleId(memory.getCouple().getId())
                .title(memory.getTitle())
                .description(memory.getDescription())
                .happenedAt(memory.getHappenedAt() != null ? memory.getHappenedAt().toLocalDate() : null)
                .locationText(memory.getLocationText())
                .mediaCount(memory.getMediaCount())
                .meta(memory.getMeta())
                .createdAt(memory.getCreatedAt())
                .updatedAt(memory.getUpdatedAt())
                .build();
    }
}
