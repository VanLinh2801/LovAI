package com.lovai.lovaiapi.dto.memory;

import lombok.Data;
import lombok.Builder;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class MemoryStatisticsResponse {
    
    private UUID coupleId;
    private long totalMemories;
    private long memoriesInDateRange;
    private long memoriesByLocation;
    private Map<String, Object> dateRangeFilter;
    private Map<String, Object> locationFilter;
    private Map<String, Long> statistics;
}
