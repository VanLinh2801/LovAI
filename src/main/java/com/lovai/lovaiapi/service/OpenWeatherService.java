package com.lovai.lovaiapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class OpenWeatherService {

    @Value("${openweather.apiKey:}")
    private String apiKey;

    @Value("${openweather.baseUrl:https://api.openweathermap.org/data/2.5}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> getWeatherForecast(Double lat, Double lon) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("OpenWeather API key is not configured");
        }

        if (lat == null || lon == null) {
            throw new IllegalArgumentException("Latitude and longitude are required");
        }

        try {
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/forecast")
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .queryParam("cnt", 40)
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            String locationName = extractLocationName(jsonNode);
            List<Map<String, Object>> simplifiedForecast = extractSimplifiedForecastList(jsonNode);

            Map<String, Object> result = new HashMap<>();
            result.put("location", locationName);
            result.put("forecast", simplifiedForecast);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weather data from OpenWeather API: " + e.getMessage(), e);
        }
    }

    private String extractLocationName(JsonNode root) {
        JsonNode cityNode = root.get("city");
        if (cityNode != null) {
            return cityNode.has("name") ? cityNode.get("name").asText() : null;
        }
        return null;
    }

    private List<Map<String, Object>> extractSimplifiedForecastList(JsonNode root) {
        List<Map<String, Object>> forecastList = new ArrayList<>();
        Map<String, Map<String, Object>> byDate = new TreeMap<>();
        JsonNode listNode = root.get("list");
        
        if (listNode != null && listNode.isArray()) {
            for (JsonNode item : listNode) {
                Map<String, Object> simplified = extractSimplifiedForecastItem(item);
                String date = (String) simplified.get("targetDate");
                if (date == null) continue;
                if (!byDate.containsKey(date)) {
                    byDate.put(date, simplified);
                    if (byDate.size() >= 7) break;
                }
            }
        }

        forecastList.addAll(byDate.values());
        return forecastList;
    }
    
    public Map<String, Object> getWeatherForecastAtTime(Double lat, Double lon, LocalDateTime targetDateTime) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("OpenWeather API key is not configured");
        }

        if (lat == null || lon == null) {
            throw new IllegalArgumentException("Latitude and longitude are required");
        }
        
        if (targetDateTime == null) {
            throw new IllegalArgumentException("Target datetime is required");
        }

        try {
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/forecast")
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .queryParam("cnt", 40)
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            long targetTimestamp = targetDateTime.toEpochSecond(ZoneOffset.UTC);
            
            Map<String, Object> closestForecast = findClosestForecast(jsonNode, targetTimestamp);
            
            Map<String, Object> result = new HashMap<>();
            result.put("location", extractLocationName(jsonNode));
            result.put("targetDate", closestForecast.get("targetDate"));
            result.put("forecast", closestForecast.get("forecast"));

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weather data from OpenWeather API: " + e.getMessage(), e);
        }
    }
    
    private Map<String, Object> findClosestForecast(JsonNode root, long targetTimestamp) {
        JsonNode listNode = root.get("list");
        Map<String, Object> closestForecast = null;
        long minDiff = Long.MAX_VALUE;
        
        if (listNode != null && listNode.isArray()) {
            for (JsonNode item : listNode) {
                if (item.has("dt")) {
                    long itemTimestamp = item.get("dt").asLong();
                    long diff = Math.abs(itemTimestamp - targetTimestamp);
                    
                    if (diff < minDiff) {
                        minDiff = diff;
                        closestForecast = extractSimplifiedForecastItem(item);
                    }
                }
            }
        }
        
        return closestForecast != null ? closestForecast : new HashMap<>();
    }
    
    private Map<String, Object> extractSimplifiedForecastItem(JsonNode item) {
        Map<String, Object> result = new HashMap<>();

        String dateText = null;
        if (item.has("dt_txt")) {
            String dtTxt = item.get("dt_txt").asText();
            if (dtTxt != null && dtTxt.length() >= 10) {
                dateText = dtTxt.substring(0, 10);
            }
        } else if (item.has("dt")) {
            long epoch = item.get("dt").asLong();
            dateText = LocalDateTime.ofEpochSecond(epoch, 0, ZoneOffset.UTC).toLocalDate().toString();
        }
        result.put("targetDate", dateText);

        Map<String, Object> forecast = new HashMap<>();

        if (item.has("weather") && item.get("weather").isArray() && item.get("weather").size() > 0) {
            JsonNode weather = item.get("weather").get(0);
            String description = weather.has("description") ? weather.get("description").asText() : null;
            forecast.put("description", description);
        }

        if (item.has("main")) {
            JsonNode main = item.get("main");
            Double temp = main.has("temp") ? main.get("temp").asDouble() : null;
            Integer humidity = main.has("humidity") ? main.get("humidity").asInt() : null;
            forecast.put("temp", temp);
            forecast.put("humidity", humidity);
        }

        result.put("forecast", forecast);
        return result;
    }
}

