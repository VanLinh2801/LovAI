package com.lovai.lovaiapi.dto.memory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class MemoryMediaCreateRequest {
    
    @NotNull(message = "Memory ID is required")
    private UUID memoryId;
    
    @NotBlank(message = "URL is required")
    private String url;
    
    @NotBlank(message = "Media type is required")
    @Pattern(regexp = "^(image|video|audio|file)$", message = "Media type must be one of: image, video, audio, file")
    private String mediaType;
    
    private Integer width;
    private Integer height;
    private Map<String, Object> exifJson;
}
