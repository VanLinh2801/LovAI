// model/id/CoupleSettingId.java
package com.lovai.lovaiapi.model.id;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor
public class CoupleSettingId implements Serializable {
    private UUID coupleId;
    private String key;
}