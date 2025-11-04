package com.lovai.lovaiapi.dto.plan;

import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanStepResponse {
    private UUID id;
    private UUID planId;
    private Integer stepOrder;
    private String actionType;
    private Map<String, Object> placeSnapshot;
    private String note;
}


