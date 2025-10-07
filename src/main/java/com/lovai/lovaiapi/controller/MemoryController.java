package com.lovai.lovaiapi.controller;

import com.lovai.lovaiapi.dto.memory.MemoryCreateRequest;
import com.lovai.lovaiapi.dto.memory.MemoryDeleteManyRequest;
import com.lovai.lovaiapi.dto.memory.MemoryResponse;
import com.lovai.lovaiapi.dto.memory.MemoryStatisticsResponse;
import com.lovai.lovaiapi.dto.memory.MemoryUpdateRequest;
import com.lovai.lovaiapi.service.MemoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/memories")
public class MemoryController {
    
    @Autowired
    private MemoryService memoryService;
    
    @PostMapping("/create")
    public ResponseEntity<MemoryResponse> createMemory(@Valid @RequestBody MemoryCreateRequest request) {
        MemoryResponse response = memoryService.createMemory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{memoryId}")
    public ResponseEntity<MemoryResponse> getMemoryById(
            @PathVariable UUID memoryId,
            @RequestParam UUID coupleId) {
        MemoryResponse response = memoryService.getMemoryById(memoryId, coupleId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<MemoryResponse>> getMemoriesByCoupleId(@RequestParam UUID coupleId) {
        List<MemoryResponse> responses = memoryService.getMemoriesByCoupleId(coupleId);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<Page<MemoryResponse>> getMemoriesByCoupleIdWithPagination(
            @RequestParam UUID coupleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MemoryResponse> responses = memoryService.getMemoriesByCoupleIdWithPagination(coupleId, page, size);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<MemoryResponse>> searchMemories(
            @RequestParam UUID coupleId,
            @RequestParam String q) {
        List<MemoryResponse> responses = memoryService.searchMemories(coupleId, q);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/search/paginated")
    public ResponseEntity<Page<MemoryResponse>> searchMemoriesWithPagination(
            @RequestParam UUID coupleId,
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MemoryResponse> responses = memoryService.searchMemoriesWithPagination(coupleId, q, page, size);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/by-date-range")
    public ResponseEntity<List<MemoryResponse>> getMemoriesByDateRange(
            @RequestParam UUID coupleId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<MemoryResponse> responses = memoryService.getMemoriesByDateRange(coupleId, startDate, endDate);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{memoryId}")
    public ResponseEntity<MemoryResponse> updateMemory(
            @PathVariable UUID memoryId,
            @RequestParam UUID coupleId,
            @Valid @RequestBody MemoryUpdateRequest request) {
        MemoryResponse response = memoryService.updateMemory(memoryId, coupleId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{memoryId}")
    public ResponseEntity<Map<String, Object>> deleteMemory(
            @PathVariable UUID memoryId,
            @RequestParam UUID coupleId) {
        memoryService.deleteMemory(memoryId, coupleId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Memory deleted successfully"
        ));
    }
    
    @DeleteMapping("/delete-many")
    public ResponseEntity<Map<String, Object>> deleteManyMemories(@Valid @RequestBody MemoryDeleteManyRequest request) {
        Map<String, Object> result = memoryService.deleteManyMemories(request);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> countMemoriesByCoupleId(@RequestParam UUID coupleId) {
        long count = memoryService.countMemoriesByCoupleId(coupleId);
        return ResponseEntity.ok(Map.of(
            "coupleId", coupleId,
            "count", count
        ));
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<MemoryStatisticsResponse> getMemoryStatistics(
            @RequestParam UUID coupleId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String locationText) {
        MemoryStatisticsResponse response = memoryService.getMemoryStatistics(coupleId, startDate, endDate, locationText);
        return ResponseEntity.ok(response);
    }
}
