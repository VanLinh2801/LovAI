package com.lovai.lovaiapi.dto.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lovai.lovaiapi.model.enums.PlanStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class DatePlanCreateRequest {
    
    @NotNull(message = "Couple ID is required")
    private java.util.UUID coupleId;
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    
    @AssertTrue(message = "Start time must be in the future")
    private boolean isValidStartTime() {
        if (startTime == null) {
            return true; // @NotNull sẽ handle null case
        }
        return startTime.isAfter(LocalDateTime.now());
    }
    
    @AssertTrue(message = "End time must be after start time")
    private boolean isValidEndTime() {
        if (startTime == null || endTime == null) {
            return true; // Skip validation if either is null
        }
        return endTime.isAfter(startTime);
    }
    
    private PlanStatus status;
    
    @Size(max = 2000, message = "Note must not exceed 2000 characters")
    private String note;
    
    private Map<String, Object> weatherSnapshot;
    
    private Map<String, Object> meta;
    
    @Valid
    private List<PlanStepCreateRequest> planSteps;
}

