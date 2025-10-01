package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import com.lovai.lovaiapi.model.id.LocationTagId;

@Entity @Table(name="location_tags")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LocationTag {

    @EmbeddedId
    private LocationTagId id;

    @ManyToOne @MapsId("locationId")
    @JoinColumn(name="location_id")
    private Location location;

    @ManyToOne @MapsId("tagId")
    @JoinColumn(name="tag_id")
    private Tag tag;
}
