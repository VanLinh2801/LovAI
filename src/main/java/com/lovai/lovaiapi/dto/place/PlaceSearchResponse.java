package com.lovai.lovaiapi.dto.place;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceSearchResponse {
    private List<PlaceSearchItem> items;
    private int page;
    private int size;
    private int total;
}


