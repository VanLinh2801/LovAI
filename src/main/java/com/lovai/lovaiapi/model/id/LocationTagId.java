// model/id/LocationTagId.java
package com.lovai.lovaiapi.model.id;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data @NoArgsConstructor @AllArgsConstructor
public class LocationTagId implements Serializable {
    private UUID locationId;
    private UUID tagId;
}