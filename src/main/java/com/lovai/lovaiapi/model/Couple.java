// model/Couple.java
package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity @Table(name="couples")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Couple {

    @Id @UuidGenerator
    @Column(columnDefinition="uuid")
    private UUID id;

    private String title;

    @Column(name="anniversary_date")
    private LocalDate anniversaryDate;

    @ManyToOne(optional=false)
    @JoinColumn(name="user1_id", nullable=false)
    private User user1;

    @ManyToOne
    @JoinColumn(name="user2_id")
    private User user2;

    @ManyToOne
    @JoinColumn(name="partner_profile_id")
    private PartnerProfile partnerProfile;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition="jsonb", nullable=false)
    private Map<String,Object> meta;

    @Column(name="created_at", insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", insertable=false, updatable=false)
    private OffsetDateTime updatedAt;

    @Column(name="deleted_at")
    private OffsetDateTime deletedAt;
}
