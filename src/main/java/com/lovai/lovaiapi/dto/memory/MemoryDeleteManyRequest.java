package com.lovai.lovaiapi.dto.memory;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class MemoryDeleteManyRequest {
    
    @NotNull(message = "Couple ID is required")
    private UUID coupleId;
    
    @NotEmpty(message = "Memory IDs list cannot be empty")
    private List<UUID> memoryIds;
}
