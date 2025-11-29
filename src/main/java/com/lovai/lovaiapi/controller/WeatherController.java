package com.lovai.lovaiapi.controller;

import com.lovai.lovaiapi.service.OpenWeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {

    private final OpenWeatherService openWeatherService;

    public WeatherController(OpenWeatherService openWeatherService) {
        this.openWeatherService = openWeatherService;
    }

    @GetMapping("/forecast")
    public ResponseEntity<Map<String, Object>> getWeatherForecast(
            @RequestParam @NotNull(message = "Latitude is required") Double lat,
            @RequestParam @NotNull(message = "Longitude is required") Double lon,
            @RequestParam(required = false) String datetime) {
        
        if (datetime != null && !datetime.isEmpty()) {
            try {
                LocalDateTime targetDateTime = LocalDateTime.parse(datetime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                Map<String, Object> weatherData = openWeatherService.getWeatherForecastAtTime(lat, lon, targetDateTime);
                return ResponseEntity.ok(weatherData);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid datetime format. Use ISO format: yyyy-MM-ddTHH:mm:ss",
                    "message", e.getMessage()
                ));
            }
        } else {
            Map<String, Object> weatherData = openWeatherService.getWeatherForecast(lat, lon);
            return ResponseEntity.ok(weatherData);
        }
    }
}

