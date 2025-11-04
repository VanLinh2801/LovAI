package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.lovai.lovaiapi.model.enums.VenueType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity @Table(name="external_venues",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider","external_id"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ExternalVenue {

    @Id @UuidGenerator
    @Column(columnDefinition="uuid")
    private UUID id;

    @Column(nullable=false)
    private String provider;

    @Column(name="external_id", nullable=false)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(name="venue_type")
    private VenueType venueType;

    @Column(nullable=false)
    private String name;

    private String address;
    private Double lat;
    private Double lon;
    private Integer priceLevel;

    @Column(precision=2, scale=1)
    private java.math.BigDecimal rating;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="photo_urls", columnDefinition="jsonb", nullable=false)
    private List<String> photoUrls;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="provider_tags", columnDefinition="jsonb", nullable=false)
    private List<String> providerTags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="normalized_tags", columnDefinition="jsonb", nullable=false)
    private List<String> normalizedTags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="meta", columnDefinition="jsonb", nullable=false)
    private Map<String,Object> meta;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="review", columnDefinition="jsonb")
    private Map<String,Object> review;

    @Column(name="last_fetched_at")
    private OffsetDateTime lastFetchedAt;

    @Column(name="interacted_at")
    private OffsetDateTime interactedAt;

    @Column(name="created_at", insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", insertable=false, updatable=false)
    private OffsetDateTime updatedAt;
}
