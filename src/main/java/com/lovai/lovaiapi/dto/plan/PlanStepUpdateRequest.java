package com.lovai.lovaiapi.dto.plan;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class PlanStepUpdateRequest {
    
    @Min(value = 0, message = "Step order must be non-negative")
    private Integer stepOrder;
    
    private String actionType;
    
    private Map<String, Object> placeSnapshot;
    
    private UUID placeId;
    
    private String note;
}

