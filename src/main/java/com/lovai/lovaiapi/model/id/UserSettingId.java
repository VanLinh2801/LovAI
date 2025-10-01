package com.lovai.lovaiapi.model.id;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor
public class UserSettingId implements Serializable {
    private UUID userId;
    private String key;
}
