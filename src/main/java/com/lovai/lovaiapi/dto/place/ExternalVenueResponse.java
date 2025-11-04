package com.lovai.lovaiapi.dto.place;

import com.lovai.lovaiapi.model.enums.VenueType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalVenueResponse {
    private UUID id;
    private String provider;
    private String externalId;
    private VenueType venueType;
    private String name;
    private String address;
    private Double lat;
    private Double lon;
    private Integer priceLevel;
    private BigDecimal rating;
    private List<String> photoUrls;
    private List<String> providerTags;
    private List<String> normalizedTags;
    private Map<String,Object> meta;
    private OffsetDateTime interactedAt;
    private OffsetDateTime lastFetchedAt;
}


