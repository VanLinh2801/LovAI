package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import com.lovai.lovaiapi.model.enums.PreferenceKind;
import com.lovai.lovaiapi.model.id.FoodPreferenceId;

@Entity @Table(name="food_preferences")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FoodPreference {

    @EmbeddedId
    private FoodPreferenceId id;

    @ManyToOne @MapsId("userId")
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne @MapsId("tagId")
    @JoinColumn(name="tag_id")
    private Tag tag;

    @Enumerated(EnumType.STRING)
    @Column(name="preference", columnDefinition="preference_enum", nullable=false)
    private PreferenceKind preference;

    private String note;
}
