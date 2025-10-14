package com.lovai.lovaiapi.controller;

import com.lovai.lovaiapi.dto.user.UserRegisterRequest;
import com.lovai.lovaiapi.dto.user.UserResponse;
import com.lovai.lovaiapi.dto.user.UserUpdateRequest;
import com.lovai.lovaiapi.dto.user.ChangePasswordRequest;
import com.lovai.lovaiapi.dto.user.VerifyEmailRequest;
import com.lovai.lovaiapi.dto.user.LoginRequest;
import com.lovai.lovaiapi.dto.user.LoginResponse;
import com.lovai.lovaiapi.dto.user.GoogleLoginRequest;
import com.lovai.lovaiapi.dto.user.UserSearchResponse;
import com.lovai.lovaiapi.dto.notification.CreateNotificationRequest;
import com.lovai.lovaiapi.model.Notification;
import com.lovai.lovaiapi.model.User;
import com.lovai.lovaiapi.model.enums.NotifChannel;
import com.lovai.lovaiapi.service.UserService;
import com.lovai.lovaiapi.service.NotificationService;
import com.lovai.lovaiapi.service.DeliveryService;
import com.lovai.lovaiapi.repository.UserRepository;

import java.util.Map;
import java.util.UUID;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final DeliveryService deliveryService;
    private final UserRepository userRepository;

    public UserController(UserService userService, NotificationService notificationService, 
                         DeliveryService deliveryService, UserRepository userRepository) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.deliveryService = deliveryService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody UserRegisterRequest request) {
        String message = userService.register(request);
        
        try {
            User newUser = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (newUser != null) {
                CreateNotificationRequest notificationRequest = new CreateNotificationRequest();
                notificationRequest.setTitle("🎉 Chào mừng đến với LovAI!");
                notificationRequest.setBody("✨ Cảm ơn bạn đã đăng ký tài khoản! Hãy khám phá những tính năng thú vị của chúng tôi! 💕");
                notificationRequest.setChannel(NotifChannel.IN_APP);
                notificationRequest.setTemplateCode("WELCOME");
                notificationRequest.setCategory("SYSTEM");
                notificationRequest.setRecipientIds(List.of(newUser.getId()));
                
                Notification notification = notificationService.createNotification(notificationRequest);
                deliveryService.sendInAppNotification(notification);
            }
        } catch (Exception e) {
            System.err.println("Failed to send welcome notification: " + e.getMessage());
        }
        
        return ResponseEntity.ok(Map.of("message", message));
    }


    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestBody VerifyEmailRequest request) {
        userService.verifyEmail(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(Map.of("message", "Email xác minh thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, 
                                                   @Valid @RequestBody UserUpdateRequest request) {
        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Map<String, String>> changePassword(@PathVariable UUID id, 
                                                             @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request);
    
        try {
            CreateNotificationRequest notificationRequest = new CreateNotificationRequest();
            notificationRequest.setTitle("🔐 Mật khẩu đã được thay đổi");
            notificationRequest.setBody("✅ Mật khẩu tài khoản của bạn đã được thay đổi thành công. Nếu không phải bạn thực hiện, vui lòng liên hệ hỗ trợ ngay! 🚨");
            notificationRequest.setChannel(NotifChannel.IN_APP);
            notificationRequest.setTemplateCode("PASSWORD_CHANGED");
            notificationRequest.setCategory("SECURITY");
            notificationRequest.setRecipientIds(List.of(id));
            
            Notification notification = notificationService.createNotification(notificationRequest);
            deliveryService.sendInAppNotification(notification);
        } catch (Exception e) {
            System.err.println("Failed to send password change notification: " + e.getMessage());
        }
        
        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    @PostMapping("/google-login")
    public ResponseEntity<LoginResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        LoginResponse response = userService.loginWithGoogle(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<UserSearchResponse> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserSearchResponse response = userService.searchUsers(keyword, page, size);
        return ResponseEntity.ok(response);
    }
}
