// model/Location.java
package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity @Table(name="locations")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Location {

    @Id @UuidGenerator
    @Column(columnDefinition="uuid")
    private UUID id;

    @Column(nullable=false)
    private String name;

    private String address;
    private Double lat;
    private Double lon;
    private Integer priceLevel;

    @Column(precision=2, scale=1)
    private java.math.BigDecimal rating;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="tags", columnDefinition="jsonb", nullable=false)
    private List<String> tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="meta", columnDefinition="jsonb", nullable=false)
    private Map<String,Object> meta;

    @Column(name="created_at", insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", insertable=false, updatable=false)
    private OffsetDateTime updatedAt;

    @Column(name="deleted_at")
    private OffsetDateTime deletedAt;
}