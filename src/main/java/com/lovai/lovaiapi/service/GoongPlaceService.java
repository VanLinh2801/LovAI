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
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GoongPlaceService {

    @Value("${goong.apiKey:}")
    private String apiKey;

    @Value("${goong.baseUrl:https://rsapi.goong.io}")
    private String baseUrl;

    @Value("${goong.autocompletePath:/v2/place/autocomplete}")
    private String autocompletePath;

    @Value("${goong.placeDetailPath:/place/detail}")
    private String placeDetailPath;

    @Value("${goong.photoPath:/place/photo}")
    private String photoPath;

    @Value("${goong.photoMaxWidth:400}")
    private int photoMaxWidth;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ExternalVenueRepository externalVenueRepository;

    public GoongPlaceService(ExternalVenueRepository externalVenueRepository) {
        this.externalVenueRepository = externalVenueRepository;
    }

    @SuppressWarnings("unchecked")
    public PlaceSearchResponse searchPlaces(String query, int page, int size) {
        if (page < 1) page = 1;
        int maxSize = 20;
        if (size < 1) size = 10;
        if (size > maxSize) size = maxSize;
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format("%s%s?input=%s&page=%d&limit=%d&api_key=%s",
                baseUrl, autocompletePath, encoded, page, size, apiKey);

        ResponseEntity<Map> response = restTemplate.getForEntity(URI.create(url), Map.class);
        Map<?,?> body = response.getBody();
        List<Map<String, Object>> predictions = new ArrayList<>();
        int total = 0;
        if (body != null) {
            Object preds = body.get("predictions");
            if (preds instanceof List<?> list) {
                for (Object o : list) {
                    if (o instanceof Map<?,?> m) {
                        predictions.add(new HashMap<>((Map<String, Object>) m));
                    }
                }
            }
            Object totalObj = body.get("total");
            if (totalObj instanceof Number n) total = n.intValue();
        }

        List<PlaceSearchItem> items = predictions.stream().map(pred -> {
            String placeId = stringVal(pred.get("place_id"));
            String name = stringVal(pred.getOrDefault("structured_formatting", new HashMap<>()) instanceof Map sf ? sf.get("main_text") : pred.get("description"));
            String address = stringVal(pred.get("description"));
            DetailSummary summary = fetchDetailSummary(placeId);
            Double lat = summary.lat;
            Double lon = summary.lon;
            String thumbnailUrl = summary.thumbnailUrl;
            return PlaceSearchItem.builder()
                    .placeId(placeId)
                    .name(name)
                    .address(address)
                    .lat(lat)
                    .lon(lon)
                    .thumbnailUrl(thumbnailUrl)
                    .build();
        }).collect(Collectors.toList());

        return PlaceSearchResponse.builder()
                .items(items)
                .page(page)
                .size(size)
                .total(total)
                .build();
    }

    public List<PlaceSearchItem> searchPlaces(String query) {
        return searchPlaces(query, 15);
    }

    public List<PlaceSearchItem> searchPlaces(String query, int limit) {
        int maxLimit = 50;
        if (limit < 1) limit = 15;
        if (limit > maxLimit) limit = maxLimit;
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = String.format("%s%s?input=%s&api_key=%s",
                baseUrl, autocompletePath, encoded, apiKey);

        ResponseEntity<Map> response = restTemplate.getForEntity(URI.create(url), Map.class);
        Map<?,?> body = response.getBody();
        List<Map<String, Object>> predictions = new ArrayList<>();
        if (body != null) {
            Object preds = body.get("predictions");
            if (preds instanceof List<?> list) {
                for (Object o : list) {
                    if (o instanceof Map<?,?> m) {
                        predictions.add(new HashMap<>((Map<String, Object>) m));
                    }
                }
            }
        }

        return predictions.stream().limit(limit).map(pred -> {
            String placeId = stringVal(pred.get("place_id"));
            String name = stringVal(pred.getOrDefault("structured_formatting", new HashMap<>()) instanceof Map sf ? sf.get("main_text") : pred.get("description"));
            String address = stringVal(pred.get("description"));
            DetailSummary summary = fetchDetailSummary(placeId);
            Double lat = summary.lat;
            Double lon = summary.lon;
            String thumbnailUrl = summary.thumbnailUrl;
            return PlaceSearchItem.builder()
                    .placeId(placeId)
                    .name(name)
                    .address(address)
                    .lat(lat)
                    .lon(lon)
                    .thumbnailUrl(thumbnailUrl)
                    .build();
        }).collect(java.util.stream.Collectors.toList());
    }

    public PlaceDetailResponse getPlaceDetail(String placeId) {
        String detailUrl = String.format("%s%s?place_id=%s&api_key=%s", baseUrl, placeDetailPath, placeId, apiKey);
        ResponseEntity<Map> response = restTemplate.getForEntity(URI.create(detailUrl), Map.class);
        Map<?,?> body = response.getBody();
        if (body == null) return null;
        Object result = body.get("result");
        if (!(result instanceof Map<?,?> res)) return null;

        String name = stringVal(res.get("name"));
        String address = stringVal(res.get("formatted_address"));
        Double lat = null, lon = null;
        Object geometry = res.get("geometry");
        if (geometry instanceof Map<?,?> g) {
            Object location = g.get("location");
            if (location instanceof Map<?,?> loc) {
                lat = doubleVal(loc.get("lat"));
                lon = doubleVal(loc.get("lng"));
            }
        }
        Integer priceLevel = res.get("price_level") instanceof Number n ? n.intValue() : null;
        java.math.BigDecimal rating = null;
        if (res.get("rating") instanceof Number rn) rating = new java.math.BigDecimal(rn.toString());
        String phone = stringVal(res.get("formatted_phone_number"));
        String website = stringVal(res.get("website"));
        List<String> types = new ArrayList<>();
        Object typesObj = res.get("types");
        if (typesObj instanceof List<?> tl) {
            for (Object o : tl) if (o != null) types.add(String.valueOf(o));
        }
        List<String> photoUrls = new ArrayList<>();
        Object photos = res.get("photos");
        if (photos instanceof List<?> plist) {
            for (Object p : plist) {
                if (p instanceof Map<?,?> pm) {
                    Object photoUrl = pm.get("photo_url");
                    if (photoUrl instanceof String s && !s.isBlank()) photoUrls.add(s);
                    else {
                        Object url = pm.get("url");
                        if (url instanceof String s2 && !s2.isBlank()) photoUrls.add(s2);
                        else {
                            Object ref = pm.get("photo_reference");
                            if (ref instanceof String pr && !pr.isBlank()) {
                                photoUrls.add(buildPhotoUrl(pr));
                            }
                        }
                    }
                }
            }
        }

        // persist ExternalVenue (upsert by provider+externalId)
        String provider = "goong";
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
        venue.setProviderTags(types.isEmpty() ? new ArrayList<>() : types);
        venue.setNormalizedTags(new ArrayList<>()); // Will be normalized later if needed
        venue.setPhotoUrls(photoUrls.isEmpty() ? new ArrayList<>() : photoUrls);
        if (venue.getMeta() == null) {
            venue.setMeta(new HashMap<>());
        }
        // Extract most_relevant from user_reviews and save directly to review
        Map<String, Object> reviewData = null;
        Object userReviews = res.get("user_reviews");
        if (userReviews instanceof Map<?,?> urMap) {
            Object mostRelevant = urMap.get("most_relevant");
            if (mostRelevant != null) {
                if (mostRelevant instanceof Map<?,?>) {
                    reviewData = new HashMap<>((Map<String, Object>) mostRelevant);
                } else if (mostRelevant instanceof List<?>) {
                    // If it's a list, wrap it in a map
                    reviewData = new HashMap<>();
                    reviewData.put("reviews", mostRelevant);
                }
            }
        }
        venue.setReview(reviewData);
        venue.setLastFetchedAt(java.time.OffsetDateTime.now());
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
                .review(reviewData)
                .build();
    }

    private DetailSummary fetchDetailSummary(String placeId) {
        DetailSummary summary = new DetailSummary();
        if (placeId == null || placeId.isBlank()) return summary;
        String detailUrl = String.format("%s%s?place_id=%s&api_key=%s", baseUrl, placeDetailPath, placeId, apiKey);
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(URI.create(detailUrl), Map.class);
            Map<?,?> body = response.getBody();
            if (body == null) return summary;
            Object result = body.get("result");
            if (!(result instanceof Map<?,?> res)) return summary;
            Object geometry = res.get("geometry");
            if (geometry instanceof Map<?,?> g) {
                Object location = g.get("location");
                if (location instanceof Map<?,?> loc) {
                    summary.lat = doubleVal(loc.get("lat"));
                    summary.lon = doubleVal(loc.get("lng"));
                }
            }
            Object photos = res.get("photos");
            if (photos instanceof List<?> list && !list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof Map<?,?> p) {
                    Object photoUrl = p.get("photo_url");
                    if (photoUrl instanceof String s && !s.isBlank()) summary.thumbnailUrl = s;
                    else {
                        Object url = p.get("url");
                        if (url instanceof String s2 && !s2.isBlank()) summary.thumbnailUrl = s2;
                        else {
                            Object ref = p.get("photo_reference");
                            if (ref instanceof String pr && !pr.isBlank()) summary.thumbnailUrl = buildPhotoUrl(pr);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return summary;
    }

    private static class DetailSummary {
        Double lat;
        Double lon;
        String thumbnailUrl;
    }

    private String buildPhotoUrl(String photoReference) {
        return String.format("%s%s?photo_reference=%s&maxwidth=%d&api_key=%s",
                baseUrl, photoPath, URLEncoder.encode(photoReference, StandardCharsets.UTF_8), photoMaxWidth, apiKey);
    }

    private static String stringVal(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static Double doubleVal(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        try {
            return o == null ? null : Double.parseDouble(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }
}


