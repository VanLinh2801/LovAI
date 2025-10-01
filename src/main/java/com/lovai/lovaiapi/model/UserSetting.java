package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.lovai.lovaiapi.model.id.UserSettingId;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity @Table(name="user_settings")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserSetting {

    @EmbeddedId
    private UserSettingId id;

    @ManyToOne @MapsId("userId")
    @JoinColumn(name="user_id")
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="value", columnDefinition="jsonb", nullable=false)
    private Map<String,Object> value;

    @Column(name="updated_at", insertable=false, updatable=false)
    private OffsetDateTime updatedAt;
}
