package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import com.lovai.lovaiapi.model.id.UserNotificationMuteId;

import java.time.OffsetDateTime;

@Entity @Table(name="user_notification_mutes")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserNotificationMute {

    @EmbeddedId
    private UserNotificationMuteId id;

    @ManyToOne @MapsId("userId")
    @JoinColumn(name="user_id")
    private User user;

    @Column(name="muted_until")
    private OffsetDateTime mutedUntil;
}
