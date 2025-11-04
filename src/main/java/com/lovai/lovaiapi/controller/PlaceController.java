package com.lovai.lovaiapi.controller;

import com.lovai.lovaiapi.dto.place.PlaceDetailResponse;
import com.lovai.lovaiapi.dto.place.PlaceSearchItem;
import com.lovai.lovaiapi.dto.place.PlaceSearchResponse;
import com.lovai.lovaiapi.service.GoongPlaceService;
import com.lovai.lovaiapi.service.SerpApiPlaceService;
import org.springframework.beans.factory.annotation.Value;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/places")
public class PlaceController {

    private final GoongPlaceService goongPlaceService;
    private final SerpApiPlaceService serpApiPlaceService;
    
    @Value("${places.provider}")
    private String placesProvider;

    public PlaceController(GoongPlaceService goongPlaceService, SerpApiPlaceService serpApiPlaceService) {
        this.goongPlaceService = goongPlaceService;
        this.serpApiPlaceService = serpApiPlaceService;
    }

    @GetMapping("/search")
    public ResponseEntity<PlaceSearchResponse> search(
            @RequestParam("search_key") String searchKey,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "lat", required = false) Double lat,
            @RequestParam(name = "lon", required = false) Double lon) {
        PlaceSearchResponse result = useSerp() ?
                serpApiPlaceService.searchPlaces(searchKey, page, size, lat, lon) :
                goongPlaceService.searchPlaces(searchKey, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search-no-pagination")
    public ResponseEntity<List<PlaceSearchItem>> searchNoPagination(
            @RequestParam("search_key") String searchKey,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "lat", required = false) Double lat,
            @RequestParam(name = "lon", required = false) Double lon) {
        List<PlaceSearchItem> items = (limit == null)
                ? (useSerp() ? serpApiPlaceService.searchPlaces(searchKey, lat, lon) : goongPlaceService.searchPlaces(searchKey))
                : (useSerp() ? serpApiPlaceService.searchPlaces(searchKey, limit, lat, lon) : goongPlaceService.searchPlaces(searchKey, limit));
        return ResponseEntity.ok(items);
    }

    @GetMapping("/detail")
    public ResponseEntity<PlaceDetailResponse> getDetail(
            @RequestParam("place_id") String placeId) {
        PlaceDetailResponse detail = useSerp() ?
                serpApiPlaceService.getPlaceDetail(placeId) :
                goongPlaceService.getPlaceDetail(placeId);
        return ResponseEntity.ok(detail);
    }

    private boolean useSerp() { return "serpapi".equalsIgnoreCase(placesProvider); }
}
