package com.lovai.lovaiapi.controller;

import com.lovai.lovaiapi.dto.notification.CreateNotificationRequest;
import com.lovai.lovaiapi.dto.plan.DatePlanCreateRequest;
import com.lovai.lovaiapi.dto.plan.DatePlanResponse;
import com.lovai.lovaiapi.dto.plan.DatePlanUpdateRequest;
import com.lovai.lovaiapi.dto.plan.PlanStepCreateRequest;
import com.lovai.lovaiapi.dto.plan.PlanStepResponse;
import com.lovai.lovaiapi.model.Couple;
import com.lovai.lovaiapi.model.Notification;
import com.lovai.lovaiapi.model.enums.NotifChannel;
import com.lovai.lovaiapi.repository.CoupleRepository;
import com.lovai.lovaiapi.service.DatePlanService;
import com.lovai.lovaiapi.service.DeliveryService;
import com.lovai.lovaiapi.service.NotificationService;
import com.lovai.lovaiapi.repository.NotificationRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/date-plans")
public class DatePlanController {
    
    @Autowired
    private DatePlanService datePlanService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private DeliveryService deliveryService;
    
    @Autowired
    private CoupleRepository coupleRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @PostMapping("/create")
    public ResponseEntity<DatePlanResponse> createDatePlan(@Valid @RequestBody DatePlanCreateRequest request) {
        DatePlanResponse response = datePlanService.createDatePlan(request);
        
        sendDatePlanCreatedNotification(response);
        if (response.getStartTime() != null) {
            createStartTimeNotification(response);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    private void sendDatePlanCreatedNotification(DatePlanResponse datePlan) {
        try {
            Couple couple = coupleRepository.findById(datePlan.getCoupleId())
                    .orElse(null);
            
            if (couple == null) {
                return;
            }
            
            List<UUID> recipientIds = new ArrayList<>();
            if (couple.getUser1() != null) {
                recipientIds.add(couple.getUser1().getId());
            }
            if (couple.getUser2() != null) {
                recipientIds.add(couple.getUser2().getId());
            }
            
            if (recipientIds.isEmpty()) {
                return;
            }
            
            CreateNotificationRequest notificationRequest = new CreateNotificationRequest();
            notificationRequest.setTitle("📅 Date Plan đã được tạo!");
            notificationRequest.setBody(String.format("✨ Bạn có một date plan mới: %s", datePlan.getTitle()));
            notificationRequest.setChannel(NotifChannel.IN_APP);
            notificationRequest.setTemplateCode("DATE_PLAN_CREATED");
            notificationRequest.setCategory("DATE_PLAN");
            notificationRequest.setRecipientIds(recipientIds);
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("planId", datePlan.getId().toString());
            payload.put("planTitle", datePlan.getTitle());
            payload.put("startTime", datePlan.getStartTime() != null ? datePlan.getStartTime().toString() : null);
            payload.put("endTime", datePlan.getEndTime() != null ? datePlan.getEndTime().toString() : null);
            notificationRequest.setPayload(payload);
            
            Notification notification = notificationService.createNotification(notificationRequest);
            deliveryService.sendInAppNotification(notification);
        } catch (Exception e) {
            System.err.println("Failed to send date plan creation notification: " + e.getMessage());
            // Don't throw exception to avoid breaking the main flow
        }
    }
    
    private void createStartTimeNotification(DatePlanResponse datePlan) {
        try {
            Couple couple = coupleRepository.findById(datePlan.getCoupleId())
                    .orElse(null);
            
            if (couple == null) {
                return;
            }
            
            OffsetDateTime scheduledAt = datePlan.getStartTime()
                    .atZone(ZoneId.systemDefault())
                    .toOffsetDateTime();
            if (scheduledAt.isBefore(OffsetDateTime.now())) {
                return;
            }
            List<UUID> recipientIds = new ArrayList<>();
            if (couple.getUser1() != null) {
                recipientIds.add(couple.getUser1().getId());
            }
            if (couple.getUser2() != null) {
                recipientIds.add(couple.getUser2().getId());
            }
            
            if (recipientIds.isEmpty()) {
                return;
            }
            
            CreateNotificationRequest notificationRequest = new CreateNotificationRequest();
            notificationRequest.setTitle("⏰ Đã đến giờ Date Plan!");
            notificationRequest.setBody(String.format("🎉 Đã đến giờ bắt đầu date plan: %s. Hãy chuẩn bị nhé! 💕", datePlan.getTitle()));
            notificationRequest.setChannel(NotifChannel.IN_APP);
            notificationRequest.setTemplateCode("DATE_PLAN_START_TIME");
            notificationRequest.setCategory("DATE_PLAN");
            notificationRequest.setRecipientIds(recipientIds);
            notificationRequest.setScheduledAt(scheduledAt);
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("planId", datePlan.getId().toString());
            payload.put("planTitle", datePlan.getTitle());
            payload.put("startTime", datePlan.getStartTime().toString());
            payload.put("endTime", datePlan.getEndTime() != null ? datePlan.getEndTime().toString() : null);
            notificationRequest.setPayload(payload);
            
            notificationService.createNotification(notificationRequest);
        } catch (Exception e) {
            System.err.println("Failed to create start time notification: " + e.getMessage());
        }
    }
    
    private void rescheduleStartTimeNotification(DatePlanResponse datePlan) {
        try {
            OffsetDateTime newScheduledAt = datePlan.getStartTime()
                    .atZone(ZoneId.systemDefault())
                    .toOffsetDateTime();
            if (newScheduledAt.isBefore(OffsetDateTime.now())) {
                return;
            }
            List<com.lovai.lovaiapi.model.Notification> notifs = notificationRepository
                    .findScheduledByTemplateAndPlanId("DATE_PLAN_START_TIME", "PENDING", datePlan.getId().toString());
            if (notifs == null || notifs.isEmpty()) {
                createStartTimeNotification(datePlan);
                return;
            }
            for (com.lovai.lovaiapi.model.Notification n : notifs) {
                n.setScheduledAt(newScheduledAt);
                Map<String, Object> payload = n.getPayload() != null ? new HashMap<>(n.getPayload()) : new HashMap<>();
                payload.put("startTime", datePlan.getStartTime().toString());
                n.setPayload(payload);
                notificationRepository.save(n);
            }
        } catch (Exception e) {
            System.err.println("Failed to reschedule start time notification: " + e.getMessage());
        }
    }
    
    @GetMapping("/{planId}")
    public ResponseEntity<DatePlanResponse> getDatePlanById(
            @PathVariable UUID planId,
            @RequestParam UUID coupleId) {
        DatePlanResponse response = datePlanService.getDatePlanById(planId, coupleId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<DatePlanResponse>> getDatePlansByCoupleId(@RequestParam UUID coupleId) {
        List<DatePlanResponse> responses = datePlanService.getDatePlansByCoupleId(coupleId);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{planId}")
    public ResponseEntity<DatePlanResponse> updateDatePlan(
            @PathVariable UUID planId,
            @RequestParam UUID coupleId,
            @Valid @RequestBody DatePlanUpdateRequest request) {
        // Fetch old plan to compare startTime
        DatePlanResponse oldPlan = datePlanService.getDatePlanById(planId, coupleId);
        DatePlanResponse response = datePlanService.updateDatePlan(planId, coupleId, request);
        
        // If startTime changed, reschedule the start notification
        if (request.getStartTime() != null && (oldPlan.getStartTime() == null || !request.getStartTime().equals(oldPlan.getStartTime()))) {
            rescheduleStartTimeNotification(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{planId}")
    public ResponseEntity<Map<String, Object>> deleteDatePlan(
            @PathVariable UUID planId,
            @RequestParam UUID coupleId) {
        datePlanService.deleteDatePlan(planId, coupleId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Date plan deleted successfully"
        ));
    }
    
    @PostMapping("/steps/{planId}")
    public ResponseEntity<PlanStepResponse> addStepToDatePlan(
            @PathVariable UUID planId,
            @RequestParam UUID coupleId,
            @Valid @RequestBody PlanStepCreateRequest request) {
        PlanStepResponse response = datePlanService.addStepToDatePlan(planId, coupleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @DeleteMapping("/steps/{planId}/{stepId}")
    public ResponseEntity<Map<String, Object>> deleteStepFromDatePlan(
            @PathVariable UUID planId,
            @PathVariable UUID stepId,
            @RequestParam UUID coupleId) {
        datePlanService.deleteStepFromDatePlan(planId, coupleId, stepId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Plan step deleted successfully"
        ));
    }
}

