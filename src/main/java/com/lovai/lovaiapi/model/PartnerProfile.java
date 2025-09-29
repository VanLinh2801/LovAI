// model/PartnerProfile.java
package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.lovai.lovaiapi.model.enums.Gender;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity @Table(name="partner_profiles")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PartnerProfile {

    @Id @UuidGenerator
    @Column(columnDefinition="uuid")
    private UUID id;

    @OneToOne
    @JoinColumn(name="user_id", unique = true)
    private User user;

    @Column(nullable=false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition="gender_enum")
    private Gender gender;

    @Column(name="date_of_birth")
    private LocalDate dateOfBirth;

    private String phone;

    @Column
    private String email;

    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition="jsonb", nullable=false)
    private Map<String,Object> meta;

    @Column(name="created_at", insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", insertable=false, updatable=false)
    private OffsetDateTime updatedAt;
}
