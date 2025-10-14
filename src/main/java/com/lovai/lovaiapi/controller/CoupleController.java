package com.lovai.lovaiapi.controller;

import com.lovai.lovaiapi.dto.couple.CreateCoupleRequest;
import com.lovai.lovaiapi.dto.couple.CreateCoupleWithPartnerRequest;
import com.lovai.lovaiapi.dto.couple.CreateCoupleInviteRequest;
import com.lovai.lovaiapi.dto.couple.CoupleResponse;
import com.lovai.lovaiapi.dto.couple.CoupleInviteResponse;
import com.lovai.lovaiapi.dto.couple.UpdateCoupleRequest;
import com.lovai.lovaiapi.dto.notification.CreateNotificationRequest;
import com.lovai.lovaiapi.model.Notification;
import com.lovai.lovaiapi.model.enums.NotifChannel;
import com.lovai.lovaiapi.service.CoupleService;
import com.lovai.lovaiapi.service.NotificationService;
import com.lovai.lovaiapi.service.DeliveryService;
import com.lovai.lovaiapi.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/couples")
public class CoupleController {

    private final CoupleService coupleService;
    private final NotificationService notificationService;
    private final DeliveryService deliveryService;
    private final UserService userService;

    public CoupleController(CoupleService coupleService, NotificationService notificationService, 
                           DeliveryService deliveryService, UserService userService) {
        this.coupleService = coupleService;
        this.notificationService = notificationService;
        this.deliveryService = deliveryService;
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<CoupleResponse> createCouple(@Valid @RequestBody CreateCoupleRequest request) {
        CoupleResponse response = coupleService.createCouple(request);
        
        try {
            CreateNotificationRequest notificationRequest = new CreateNotificationRequest();
            notificationRequest.setTitle("💕 Couple đã được tạo thành công!");
            notificationRequest.setBody("🎉 Bạn đã tạo couple thành công! Hãy bắt đầu chia sẻ những kỷ niệm đẹp! ✨");
            notificationRequest.setChannel(NotifChannel.IN_APP);
            notificationRequest.setTemplateCode("COUPLE_CREATED");
            notificationRequest.setCategory("COUPLE");
            notificationRequest.setRecipientIds(List.of(request.getUser1Id()));
            
            Notification notification = notificationService.createNotification(notificationRequest);
            deliveryService.sendInAppNotification(notification);
        } catch (Exception e) {
            System.err.println("Failed to send couple creation notification: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/with-partner")
    public ResponseEntity<CoupleResponse> createCoupleWithPartner(@Valid @RequestBody CreateCoupleWithPartnerRequest request) {
        CoupleResponse response = coupleService.createCoupleWithPartner(request);
        
        try {
            CreateNotificationRequest notificationRequest = new CreateNotificationRequest();
            notificationRequest.setTitle("💕 Couple đã được tạo thành công!");
            notificationRequest.setBody("🎉 Bạn và " + request.getPartner().getName() + " đã trở thành couple! Hãy bắt đầu chia sẻ những kỷ niệm đẹp! ✨");
            notificationRequest.setChannel(NotifChannel.IN_APP);
            notificationRequest.setTemplateCode("COUPLE_CREATED_WITH_PARTNER");
            notificationRequest.setCategory("COUPLE");
            notificationRequest.setRecipientIds(List.of(request.getUserId()));
            
            Notification notification = notificationService.createNotification(notificationRequest);
            deliveryService.sendInAppNotification(notification);
        } catch (Exception e) {
            System.err.println("Failed to send couple creation with partner notification: " + e.getMessage());
        }
        
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
        
        try {
            List<UUID> recipientIds = List.of(response.getUser1().getId(), response.getUser2().getId());
            
            CreateNotificationRequest notificationRequest = new CreateNotificationRequest();
            notificationRequest.setTitle("📝 Thông tin couple đã được cập nhật");
            notificationRequest.setBody("✅ Thông tin couple của bạn đã được cập nhật thành công! 💕");
            notificationRequest.setChannel(NotifChannel.IN_APP);
            notificationRequest.setTemplateCode("COUPLE_UPDATED");
            notificationRequest.setCategory("COUPLE");
            notificationRequest.setRecipientIds(recipientIds);
            
            Notification notification = notificationService.createNotification(notificationRequest);
            deliveryService.sendInAppNotification(notification);
        } catch (Exception e) {
            System.err.println("Failed to send couple update notification: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/invite")
    public ResponseEntity<CoupleInviteResponse> createCoupleInvite(@RequestParam UUID inviterId,
                                                                  @Valid @RequestBody CreateCoupleInviteRequest request) {
        CoupleInviteResponse response = coupleService.createCoupleInvite(inviterId, request);
        
        try {
            CreateNotificationRequest notificationRequest = new CreateNotificationRequest();
            notificationRequest.setTitle("💌 Bạn có lời mời couple mới!");
            notificationRequest.setBody("💕 " + userService.getUserById(inviterId).getName() + " đã mời bạn tham gia couple! Hãy kiểm tra và phản hồi lời mời! ✨");
            notificationRequest.setChannel(NotifChannel.IN_APP);
            notificationRequest.setTemplateCode("COUPLE_INVITE");
            notificationRequest.setCategory("COUPLE");
            notificationRequest.setRecipientIds(List.of(request.getInviteeId()));
            
            Notification notification = notificationService.createNotification(notificationRequest);
            deliveryService.sendInAppNotification(notification);
        } catch (Exception e) {
            System.err.println("Failed to send couple invite notification: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> leaveCouple(@PathVariable UUID id, 
                                                          @RequestParam UUID userId) {
        CoupleResponse coupleInfo = coupleService.getCoupleByUser(userId);
        UUID partnerId = coupleInfo.getUser1().getId().equals(userId) ? coupleInfo.getUser2().getId() : coupleInfo.getUser1().getId();
        
        coupleService.leaveCouple(id, userId);
        
        try {
            CreateNotificationRequest notificationRequest = new CreateNotificationRequest();
            notificationRequest.setTitle("💔 Partner đã rời couple");
            notificationRequest.setBody(userService.getUserById(userId).getName() + " đã rời couple 💔 Chuyện gì đã xảy ra vậy 🥺?");
            notificationRequest.setChannel(NotifChannel.IN_APP);
            notificationRequest.setTemplateCode("COUPLE_LEFT");
            notificationRequest.setCategory("COUPLE");
            notificationRequest.setRecipientIds(List.of(partnerId));
            
            Notification notification = notificationService.createNotification(notificationRequest);
            deliveryService.sendInAppNotification(notification);
        } catch (Exception e) {
            System.err.println("Failed to send couple leave notification: " + e.getMessage());
        }
        
        return ResponseEntity.ok(Map.of("message", "Rời couple thành công"));
    }
}
