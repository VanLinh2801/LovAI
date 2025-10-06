package com.lovai.lovaiapi.controller;

import com.lovai.lovaiapi.dto.push.PushTokenRequest;
import com.lovai.lovaiapi.service.PushTokenService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/push-tokens")
public class PushTokenController {

    private final PushTokenService pushTokenService;

    public PushTokenController(PushTokenService pushTokenService) {
        this.pushTokenService = pushTokenService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> registerPushToken(
            @Valid @RequestBody PushTokenRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        
        pushTokenService.createOrUpdateToken(
            userId, 
            request.getToken(), 
            request.getPlatform(), 
            request.getDeviceId()
        );
        
        return ResponseEntity.ok(Map.of("message", "Push token registered successfully"));
    }

    @DeleteMapping("/{token}")
    public ResponseEntity<Map<String, String>> deactivatePushToken(@PathVariable String token) {
        pushTokenService.deactivateToken(token);
        return ResponseEntity.ok(Map.of("message", "Push token deactivated successfully"));
    }

    @PutMapping("/update-last-seen")
    public ResponseEntity<Map<String, String>> updateLastSeen(@RequestHeader("X-User-Id") UUID userId) {
        pushTokenService.updateLastSeenAt(userId);
        return ResponseEntity.ok(Map.of("message", "Last seen updated successfully"));
    }

    @DeleteMapping("/user/all")
    public ResponseEntity<Map<String, String>> deactivateAllUserTokens(@RequestHeader("X-User-Id") UUID userId) {
        pushTokenService.deactivateAllTokensByUserId(userId);
        return ResponseEntity.ok(Map.of("message", "All push tokens deactivated successfully"));
    }
}

