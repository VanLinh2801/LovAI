package com.lovai.lovaiapi.dto.activity;

import com.lovai.lovaiapi.model.enums.ActivityAction;
import java.time.OffsetDateTime;
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
public class ActivityLogResponse {
    private UUID id;
    private UUID userId;
    private ActivityAction action;
    private String targetType;
    private String provider;
    private String externalId;
    private Map<String, Object> props;
    private OffsetDateTime occurredAt;
}


