// model/AppSetting.java
package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity @Table(name="app_settings")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AppSetting {

    @Id
    private String key;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="value", columnDefinition="jsonb", nullable=false)
    private Map<String,Object> value;

    @Column(name="updated_at", insertable=false, updatable=false)
    private OffsetDateTime updatedAt;
}