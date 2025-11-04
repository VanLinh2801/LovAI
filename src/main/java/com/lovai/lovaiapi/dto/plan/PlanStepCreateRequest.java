package com.lovai.lovaiapi.dto.plan;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class PlanStepCreateRequest {
    
    @NotNull(message = "Step order is required")
    @Min(value = 0, message = "Step order must be non-negative")
    private Integer stepOrder;
    
    private String actionType;
    
    @NotNull(message = "Place snapshot is required")
    private Map<String, Object> placeSnapshot;
    
    private UUID placeId;
    
    private String note;
}

