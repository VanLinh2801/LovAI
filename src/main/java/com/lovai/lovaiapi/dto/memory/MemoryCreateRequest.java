package com.lovai.lovaiapi.dto.memory;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
public class MemoryCreateRequest {
    
    @NotNull(message = "Couple ID is required")
    private java.util.UUID coupleId;
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate happenedAt;
    
    @Size(max = 500, message = "Location text must not exceed 500 characters")
    private String locationText;
    
    private Map<String, Object> meta;
}
