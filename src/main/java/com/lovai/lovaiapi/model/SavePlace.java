package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "save_place",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "venue_id"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SavePlace {

    @Id @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private ExternalVenue venue;

    @Column(name = "note", columnDefinition = "text")
    private String note;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}


