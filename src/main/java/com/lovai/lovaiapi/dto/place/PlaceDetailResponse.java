package com.lovai.lovaiapi.dto.place;

import com.lovai.lovaiapi.model.enums.VenueType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceDetailResponse {
    private String provider;
    private String placeId;
    private VenueType venueType;
    private String name;
    private String address;
    private Double lat;
    private Double lon;
    private Integer priceLevel;
    private BigDecimal rating;
    private String phoneNumber;
    private String website;
    private List<String> types;
    private List<String> photoUrls;
    private Map<String,Object> review;
}
