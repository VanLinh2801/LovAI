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
import com.lovai.lovaiapi.service.UserService;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody UserRegisterRequest request) {
        String message = userService.register(request);
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
