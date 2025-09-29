package com.lovai.lovaiapi.service;

import com.lovai.lovaiapi.dto.UserRegisterRequest;
import com.lovai.lovaiapi.dto.UserResponse;
import com.lovai.lovaiapi.dto.UserUpdateRequest;
import com.lovai.lovaiapi.dto.ChangePasswordRequest;
import com.lovai.lovaiapi.model.User;
import com.lovai.lovaiapi.model.EmailVerificationToken;
import com.lovai.lovaiapi.repository.UserRepository;
import com.lovai.lovaiapi.repository.EmailVerificationTokenRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       EmailVerificationTokenRepository tokenRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public String register(UserRegisterRequest request) {
        Optional<User> existing = userRepository.findByEmail(request.getEmail());

        User user;
        if (existing.isPresent()) {
            user = existing.get();
            if (user.isVerified()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
            }
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setName(request.getName());
            user.setGender(request.getGender());
            user.setDateOfBirth(request.getDateOfBirth());
        } else {
            user = new User();
            user.setEmail(request.getEmail());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setName(request.getName());
            user.setGender(request.getGender());
            user.setDateOfBirth(request.getDateOfBirth());
            user.setVerified(false);
            userRepository.save(user);
        }

        Optional<EmailVerificationToken> latestTokenOpt =
            tokenRepository.findTopByUserOrderByCreatedAtDesc(user);

        if (latestTokenOpt.isPresent()) {
            EmailVerificationToken latest = latestTokenOpt.get();
            if (!latest.isVerified() && latest.getExpiresAt().isAfter(OffsetDateTime.now())) {
                return "OTP đã được gửi, vui lòng kiểm tra email.";
            }
        }

        String otpCode = String.format("%06d", new Random().nextInt(999999));
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setOtpCode(otpCode);
        token.setExpiresAt(OffsetDateTime.now().plusMinutes(10));
        tokenRepository.save(token);

        emailService.sendOtp(user.getEmail(), otpCode);

        return "Đăng ký thành công. Vui lòng kiểm tra email để xác nhận OTP.";
    }


    @Transactional
    public void verifyEmail(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isVerified()) {
            throw new RuntimeException("Email already verified");
        }

        EmailVerificationToken token = tokenRepository
                .findTopByUserAndOtpCodeOrderByCreatedAtDesc(user, otp)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }
                

        token.setVerified(true);
        user.setVerified(true);

        tokenRepository.save(token);
        userRepository.save(user);
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setGender(user.getGender());
        response.setDateOfBirth(user.getDateOfBirth());
        return response;
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        
        userRepository.save(user);

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setGender(user.getGender());
        response.setDateOfBirth(user.getDateOfBirth());
        return response;
    }

    @Transactional
    public void changePassword(UUID id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));


        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu cũ không đúng");
        }

        // Kiểm tra mật khẩu mới có khác mật khẩu cũ không
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải khác mật khẩu cũ");
        }

        // Cập nhật mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}