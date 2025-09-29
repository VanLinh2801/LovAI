// model/PlaceSearchCache.java
package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity @Table(name="place_search_cache")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PlaceSearchCache {

    @Id @UuidGenerator
    @Column(columnDefinition="uuid")
    private UUID id;

    @Column(name="query_fingerprint", nullable=false, unique=true)
    private String queryFingerprint;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="results", columnDefinition="jsonb", nullable=false)
    private List<Object> results;

    @Column(name="ttl_seconds", nullable=false)
    private Integer ttlSeconds;

    @Column(name="created_at", insertable=false, updatable=false)
    private OffsetDateTime createdAt;
}