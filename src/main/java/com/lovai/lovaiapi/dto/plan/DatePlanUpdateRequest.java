package com.lovai.lovaiapi.dto.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lovai.lovaiapi.model.enums.PlanStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class DatePlanUpdateRequest {
    
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    
    private PlanStatus status;
    
    @Size(max = 2000, message = "Note must not exceed 2000 characters")
    private String note;
    
    private Map<String, Object> weatherSnapshot;
    
    private Map<String, Object> meta;
    
    @Valid
    private List<PlanStepUpdateRequest> planSteps;
}

