package com.lovai.lovaiapi.controller;

import com.lovai.lovaiapi.dto.couple.CreateCoupleRequest;
import com.lovai.lovaiapi.dto.couple.CreateCoupleWithPartnerRequest;
import com.lovai.lovaiapi.dto.couple.CreateCoupleInviteRequest;
import com.lovai.lovaiapi.dto.couple.CoupleResponse;
import com.lovai.lovaiapi.dto.couple.CoupleInviteResponse;
import com.lovai.lovaiapi.dto.couple.UpdateCoupleRequest;
import com.lovai.lovaiapi.service.CoupleService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/couples")
public class CoupleController {

    private final CoupleService coupleService;

    public CoupleController(CoupleService coupleService) {
        this.coupleService = coupleService;
    }

    @PostMapping("/create")
    public ResponseEntity<CoupleResponse> createCouple(@Valid @RequestBody CreateCoupleRequest request) {
        CoupleResponse response = coupleService.createCouple(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/with-partner")
    public ResponseEntity<CoupleResponse> createCoupleWithPartner(@Valid @RequestBody CreateCoupleWithPartnerRequest request) {
        CoupleResponse response = coupleService.createCoupleWithPartner(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<CoupleResponse> getMyCouple(@RequestParam UUID userId) {
        CoupleResponse response = coupleService.getCoupleByUser(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CoupleResponse> updateCouple(@PathVariable UUID id, 
                                                      @Valid @RequestBody UpdateCoupleRequest request) {
        CoupleResponse response = coupleService.updateCouple(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invite")
    public ResponseEntity<CoupleInviteResponse> createCoupleInvite(@RequestParam UUID inviterId,
                                                                  @Valid @RequestBody CreateCoupleInviteRequest request) {
        CoupleInviteResponse response = coupleService.createCoupleInvite(inviterId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> leaveCouple(@PathVariable UUID id, 
                                                          @RequestParam UUID userId) {
        coupleService.leaveCouple(id, userId);
        return ResponseEntity.ok(Map.of("message", "Rời couple thành công"));
    }
}
