// model/CoupleSetting.java
package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.lovai.lovaiapi.model.id.CoupleSettingId;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity @Table(name="couple_settings")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CoupleSetting {

    @EmbeddedId
    private CoupleSettingId id;

    @ManyToOne @MapsId("coupleId")
    @JoinColumn(name="couple_id")
    private Couple couple;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="value", columnDefinition="jsonb", nullable=false)
    private Map<String,Object> value;

    @Column(name="updated_at", insertable=false, updatable=false)
    private OffsetDateTime updatedAt;
}