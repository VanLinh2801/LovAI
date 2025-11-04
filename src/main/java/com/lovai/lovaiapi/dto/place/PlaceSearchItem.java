package com.lovai.lovaiapi.dto.place;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceSearchItem {
    private String placeId;
    private String name;
    private String address;
    private Double lat;
    private Double lon;
    private String thumbnailUrl;
}


