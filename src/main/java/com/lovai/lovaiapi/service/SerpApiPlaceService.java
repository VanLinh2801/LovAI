package com.lovai.lovaiapi.service;

import com.lovai.lovaiapi.dto.place.PlaceDetailResponse;
import com.lovai.lovaiapi.dto.place.PlaceSearchItem;
import com.lovai.lovaiapi.dto.place.PlaceSearchResponse;
import com.lovai.lovaiapi.model.ExternalVenue;
import com.lovai.lovaiapi.model.enums.VenueType;
import com.lovai.lovaiapi.repository.ExternalVenueRepository;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SerpApiPlaceService {

    @Value("${serpapi.apiKey:}")
    private String apiKey;

    @Value("${serpapi.baseUrl:https://serpapi.com}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ExternalVenueRepository externalVenueRepository;

    public SerpApiPlaceService(ExternalVenueRepository externalVenueRepository) {
        this.externalVenueRepository = externalVenueRepository;
    }

    public PlaceSearchResponse searchPlaces(String query, int page, int size, Double lat, Double lon) {
        if (page < 1) page = 1;
        int maxSize = 20;
        if (size < 1) size = 10;
        if (size > maxSize) size = maxSize;

        List<PlaceSearchItem> all = searchPlaces(query, size * page, lat, lon);
        int from = Math.min((page - 1) * size, all.size());
        int to = Math.min(from + size, all.size());
        List<PlaceSearchItem> pageItems = all.subList(from, to);
        return PlaceSearchResponse.builder()
                .items(pageItems)
                .page(page)
                .size(size)
                .total(all.size())
                .build();
    }

    public List<PlaceSearchItem> searchPlaces(String query, Double lat, Double lon) {
        return searchPlaces(query, 15, lat, lon);
    }

    public List<PlaceSearchItem> searchPlaces(String query, int limit, Double lat, Double lon) {
        int maxLimit = 50;
        if (limit < 1) limit = 15;
        if (limit > maxLimit) limit = maxLimit;
        String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url;
        if (lat != null && lon != null) {
            url = String.format("%s/search.json?engine=google_maps&type=search&q=%s&hl=vi&ll=@%s,%s,14z&api_key=%s",
                    baseUrl, q, lat, lon, apiKey);
        } else {
            // Fallback to default location if lat/lon not provided
            url = String.format("%s/search.json?engine=google_maps&type=search&q=%s&hl=vi&ll=@10.762622,106.660172,14z&api_key=%s",
                    baseUrl, q, apiKey);
        }
        ResponseEntity<Map> resp = restTemplate.getForEntity(URI.create(url), Map.class);
        Map<?,?> body = resp.getBody();
        List<PlaceSearchItem> items = new ArrayList<>();
        if (body != null) {
            // Try local_results first, then results, then organic_results
            Object results = body.get("local_results");
            if (!(results instanceof List<?>)) {
                results = body.get("results");
            }
            if (!(results instanceof List<?>)) {
                results = body.get("organic_results");
            }
            if (results instanceof List<?> list) {
                for (Object o : list) {
                    if (!(o instanceof Map<?,?> m)) continue;
                    // Prefer place_id, fallback to data_id
                    String placeId = str(m.get("place_id"));
                    if (placeId == null || placeId.isBlank()) {
                        placeId = str(m.get("data_id"));
                    }
                    String title = str(m.get("title"));
                    String address = str(m.get("address"));
                    String thumb = str(m.get("thumbnail"));
                    Double itemLat = null, itemLon = null;
                    Object gps = m.get("gps_coordinates");
                    if (gps instanceof Map<?,?> g) {
                        itemLat = dbl(g.get("latitude"));
                        itemLon = dbl(g.get("longitude"));
                    }
                    items.add(PlaceSearchItem.builder()
                            .placeId(placeId)
                            .name(title)
                            .address(address)
                            .lat(itemLat)
                            .lon(itemLon)
                            .thumbnailUrl(thumb)
                            .build());
                    if (items.size() >= limit) break;
                }
            }
        }
        return items;
    }

    public PlaceDetailResponse getPlaceDetail(String placeId) {
        // Try with type=place and place_id first, then fallback to data_id
        String url = String.format("%s/search.json?engine=google_maps&type=place&place_id=%s&hl=vi&api_key=%s",
                baseUrl, URLEncoder.encode(placeId, StandardCharsets.UTF_8), apiKey);
        ResponseEntity<Map> resp = restTemplate.getForEntity(URI.create(url), Map.class);
        Map<?,?> body = resp.getBody();
        if (body == null) {
            // Fallback: try with data_id
            url = String.format("%s/search.json?engine=google_maps&data_id=%s&hl=vi&api_key=%s",
                    baseUrl, URLEncoder.encode(placeId, StandardCharsets.UTF_8), apiKey);
            resp = restTemplate.getForEntity(URI.create(url), Map.class);
            body = resp.getBody();
        }
        if (body == null) return null;
        Object pr = body.get("place_results");
        if (!(pr instanceof Map<?,?> res)) return null;

        String name = str(res.get("title"));
        String address = str(res.get("address"));
        Double lat = null, lon = null;
        Object gps = res.get("gps_coordinates");
        if (gps instanceof Map<?,?> g) {
            lat = dbl(g.get("latitude"));
            lon = dbl(g.get("longitude"));
        }
        Integer priceLevel = null; // SerpAPI may not provide
        java.math.BigDecimal rating = null;
        if (res.get("rating" ) instanceof Number rn) rating = new java.math.BigDecimal(rn.toString());
        String phone = str(res.get("phone"));
        String website = str(res.get("website"));
        List<String> types = new ArrayList<>();
        List<String> providerTags = new ArrayList<>();
        // Extract provider tags from type field
        Object cats = res.get("type");
        if (cats instanceof String s && !s.isBlank()) {
            types.add(s);
            providerTags.add(s);
        } else if (cats instanceof List<?> catList) {
            for (Object cat : catList) {
                if (cat != null) {
                    String catStr = String.valueOf(cat);
                    types.add(catStr);
                    providerTags.add(catStr);
                }
            }
        }
        // Also check for category field
        Object category = res.get("category");
        if (category instanceof String catStr && !catStr.isBlank() && !providerTags.contains(catStr)) {
            providerTags.add(catStr);
        }
        List<String> photoUrls = new ArrayList<>();
        Object photos = res.get("photos");
        if (photos instanceof List<?> plist) {
            for (Object p : plist) {
                if (p instanceof Map<?,?> pm) {
                    String link = str(pm.get("photo_url"));
                    if (link == null || link.isBlank()) link = str(pm.get("image"));
                    if (link != null && !link.isBlank()) photoUrls.add(link);
                }
            }
        }

        // Upsert ExternalVenue
        String provider = "serpapi";
        ExternalVenue venue = externalVenueRepository
                .findByProviderAndExternalId(provider, placeId)
                .orElse(ExternalVenue.builder()
                        .provider(provider)
                        .externalId(placeId)
                        .venueType(VenueType.PLACE)
                        .meta(new HashMap<>())
                        .build());
        venue.setName(name);
        venue.setAddress(address);
        venue.setLat(lat);
        venue.setLon(lon);
        venue.setPriceLevel(priceLevel);
        venue.setRating(rating);
        venue.setProviderTags(providerTags.isEmpty() ? new ArrayList<>() : providerTags);
        venue.setNormalizedTags(new ArrayList<>()); // Will be normalized later if needed
        venue.setPhotoUrls(photoUrls.isEmpty() ? new ArrayList<>() : photoUrls);
        if (venue.getMeta() == null) {
            venue.setMeta(new HashMap<>());
        }
        venue.setLastFetchedAt(java.time.OffsetDateTime.now());
        venue.setRaw((Map<String, Object>) res);
        externalVenueRepository.save(venue);

        return PlaceDetailResponse.builder()
                .provider(provider)
                .placeId(placeId)
                .venueType(venue.getVenueType())
                .name(name)
                .address(address)
                .lat(lat)
                .lon(lon)
                .priceLevel(priceLevel)
                .rating(rating)
                .phoneNumber(phone)
                .website(website)
                .types(types)
                .photoUrls(photoUrls)
                .raw((Map<String,Object>) res)
                .build();
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }
    private static Double dbl(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        try { return o == null ? null : Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return null; }
    }
}


