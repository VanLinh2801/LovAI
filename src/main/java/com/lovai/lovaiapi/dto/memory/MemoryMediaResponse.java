package com.lovai.lovaiapi.dto.memory;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class MemoryMediaResponse {
    
    private UUID id;
    private UUID memoryId;
    private String url;
    private String mediaType;
    private Integer width;
    private Integer height;
    private Map<String, Object> exifJson;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime createdAt;
}
