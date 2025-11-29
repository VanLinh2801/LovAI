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

            Map<String, Object> result = new HashMap<>();
            result.put("location", extractLocation(jsonNode));
            result.put("forecast", extractForecast(jsonNode));
            result.put("fetchedAt", System.currentTimeMillis());

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weather data from OpenWeather API: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> extractLocation(JsonNode root) {
        Map<String, Object> location = new HashMap<>();
        JsonNode cityNode = root.get("city");
        if (cityNode != null) {
            location.put("name", cityNode.has("name") ? cityNode.get("name").asText() : null);
            location.put("country", cityNode.has("country") ? cityNode.get("country").asText() : null);
            if (cityNode.has("coord")) {
                JsonNode coord = cityNode.get("coord");
                location.put("lat", coord.has("lat") ? coord.get("lat").asDouble() : null);
                location.put("lon", coord.has("lon") ? coord.get("lon").asDouble() : null);
            }
        }
        return location;
    }

    private List<Map<String, Object>> extractForecast(JsonNode root) {
        List<Map<String, Object>> forecastList = new ArrayList<>();
        JsonNode listNode = root.get("list");
        
        if (listNode != null && listNode.isArray()) {
            for (JsonNode item : listNode) {
                Map<String, Object> forecast = new HashMap<>();
                
                forecast.put("datetime", item.has("dt") ? item.get("dt").asLong() : null);
                forecast.put("datetimeText", item.has("dt_txt") ? item.get("dt_txt").asText() : null);
                
                if (item.has("main")) {
                    JsonNode main = item.get("main");
                    Map<String, Object> mainData = new HashMap<>();
                    mainData.put("temp", main.has("temp") ? main.get("temp").asDouble() : null);
                    mainData.put("feelsLike", main.has("feels_like") ? main.get("feels_like").asDouble() : null);
                    mainData.put("tempMin", main.has("temp_min") ? main.get("temp_min").asDouble() : null);
                    mainData.put("tempMax", main.has("temp_max") ? main.get("temp_max").asDouble() : null);
                    mainData.put("pressure", main.has("pressure") ? main.get("pressure").asInt() : null);
                    mainData.put("humidity", main.has("humidity") ? main.get("humidity").asInt() : null);
                    forecast.put("main", mainData);
                }
                
                if (item.has("weather") && item.get("weather").isArray() && item.get("weather").size() > 0) {
                    JsonNode weather = item.get("weather").get(0);
                    Map<String, Object> weatherData = new HashMap<>();
                    weatherData.put("id", weather.has("id") ? weather.get("id").asInt() : null);
                    weatherData.put("main", weather.has("main") ? weather.get("main").asText() : null);
                    weatherData.put("description", weather.has("description") ? weather.get("description").asText() : null);
                    weatherData.put("icon", weather.has("icon") ? weather.get("icon").asText() : null);
                    forecast.put("weather", weatherData);
                }
                
                if (item.has("wind")) {
                    JsonNode wind = item.get("wind");
                    Map<String, Object> windData = new HashMap<>();
                    windData.put("speed", wind.has("speed") ? wind.get("speed").asDouble() : null);
                    windData.put("deg", wind.has("deg") ? wind.get("deg").asInt() : null);
                    forecast.put("wind", windData);
                }
                
                if (item.has("clouds")) {
                    JsonNode clouds = item.get("clouds");
                    Map<String, Object> cloudsData = new HashMap<>();
                    cloudsData.put("all", clouds.has("all") ? clouds.get("all").asInt() : null);
                    forecast.put("clouds", cloudsData);
                }
                
                if (item.has("rain")) {
                    JsonNode rain = item.get("rain");
                    Map<String, Object> rainData = new HashMap<>();
                    rainData.put("3h", rain.has("3h") ? rain.get("3h").asDouble() : null);
                    forecast.put("rain", rainData);
                }
                
                if (item.has("snow")) {
                    JsonNode snow = item.get("snow");
                    Map<String, Object> snowData = new HashMap<>();
                    snowData.put("3h", snow.has("3h") ? snow.get("3h").asDouble() : null);
                    forecast.put("snow", snowData);
                }
                
                forecastList.add(forecast);
            }
        }
        
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
            result.put("location", extractLocation(jsonNode));
            result.put("forecast", closestForecast);
            result.put("targetDateTime", targetDateTime.toString());
            result.put("fetchedAt", System.currentTimeMillis());

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
                        closestForecast = extractForecastItem(item);
                    }
                }
            }
        }
        
        return closestForecast != null ? closestForecast : new HashMap<>();
    }
    
    private Map<String, Object> extractForecastItem(JsonNode item) {
        Map<String, Object> forecast = new HashMap<>();
        
        forecast.put("datetime", item.has("dt") ? item.get("dt").asLong() : null);
        forecast.put("datetimeText", item.has("dt_txt") ? item.get("dt_txt").asText() : null);
        
        if (item.has("main")) {
            JsonNode main = item.get("main");
            Map<String, Object> mainData = new HashMap<>();
            mainData.put("temp", main.has("temp") ? main.get("temp").asDouble() : null);
            mainData.put("feelsLike", main.has("feels_like") ? main.get("feels_like").asDouble() : null);
            mainData.put("tempMin", main.has("temp_min") ? main.get("temp_min").asDouble() : null);
            mainData.put("tempMax", main.has("temp_max") ? main.get("temp_max").asDouble() : null);
            mainData.put("pressure", main.has("pressure") ? main.get("pressure").asInt() : null);
            mainData.put("humidity", main.has("humidity") ? main.get("humidity").asInt() : null);
            forecast.put("main", mainData);
        }
        
        if (item.has("weather") && item.get("weather").isArray() && item.get("weather").size() > 0) {
            JsonNode weather = item.get("weather").get(0);
            Map<String, Object> weatherData = new HashMap<>();
            weatherData.put("id", weather.has("id") ? weather.get("id").asInt() : null);
            weatherData.put("main", weather.has("main") ? weather.get("main").asText() : null);
            weatherData.put("description", weather.has("description") ? weather.get("description").asText() : null);
            weatherData.put("icon", weather.has("icon") ? weather.get("icon").asText() : null);
            forecast.put("weather", weatherData);
        }
        
        if (item.has("wind")) {
            JsonNode wind = item.get("wind");
            Map<String, Object> windData = new HashMap<>();
            windData.put("speed", wind.has("speed") ? wind.get("speed").asDouble() : null);
            windData.put("deg", wind.has("deg") ? wind.get("deg").asInt() : null);
            forecast.put("wind", windData);
        }
        
        if (item.has("clouds")) {
            JsonNode clouds = item.get("clouds");
            Map<String, Object> cloudsData = new HashMap<>();
            cloudsData.put("all", clouds.has("all") ? clouds.get("all").asInt() : null);
            forecast.put("clouds", cloudsData);
        }
        
        if (item.has("rain")) {
            JsonNode rain = item.get("rain");
            Map<String, Object> rainData = new HashMap<>();
            rainData.put("3h", rain.has("3h") ? rain.get("3h").asDouble() : null);
            forecast.put("rain", rainData);
        }
        
        if (item.has("snow")) {
            JsonNode snow = item.get("snow");
            Map<String, Object> snowData = new HashMap<>();
            snowData.put("3h", snow.has("3h") ? snow.get("3h").asDouble() : null);
            forecast.put("snow", snowData);
        }
        
        return forecast;
    }
}

