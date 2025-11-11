package com.lovai.lovaiapi.dto.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lovai.lovaiapi.model.enums.PlanStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class DatePlanResponse {
    
    private UUID id;
    private UUID coupleId;
    private String title;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    
    private PlanStatus status;
    private String note;
    private Integer budgetEstimate;
    private Map<String, Object> weatherSnapshot;
    private Map<String, Object> meta;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime updatedAt;
    
    private List<PlanStepResponse> planSteps;
}

