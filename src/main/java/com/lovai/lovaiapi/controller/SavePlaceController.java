package com.lovai.lovaiapi.controller;

import com.lovai.lovaiapi.dto.place.SavePlaceRequest;
import com.lovai.lovaiapi.model.SavePlace;
import com.lovai.lovaiapi.repository.UserRepository;
import com.lovai.lovaiapi.service.SavePlaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/save-places")
public class SavePlaceController {

    private final SavePlaceService savePlaceService;
    private final UserRepository userRepository;

    public SavePlaceController(SavePlaceService savePlaceService, UserRepository userRepository) {
        this.savePlaceService = savePlaceService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<SavePlace> save(@Valid @RequestBody SavePlaceRequest request) {
        UUID userId = getCurrentUserId();
        SavePlace saved = savePlaceService.savePlace(userId, request.getVenueId(), request.getNote());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{venueId}")
    public ResponseEntity<Void> unsave(@PathVariable UUID venueId) {
        UUID userId = getCurrentUserId();
        savePlaceService.removeSave(userId, venueId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<SavePlace>> list() {
        UUID userId = getCurrentUserId();
        List<SavePlace> saves = savePlaceService.listSaves(userId);
        return ResponseEntity.ok(saves);
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth != null ? auth.getName() : null);
        return userRepository.findByEmail(email)
                .map(u -> u.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}


