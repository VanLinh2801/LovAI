package com.lovai.lovaiapi.service;

import com.lovai.lovaiapi.dto.user.UserRegisterRequest;
import com.lovai.lovaiapi.dto.user.UserResponse;
import com.lovai.lovaiapi.dto.user.UserUpdateRequest;
import com.lovai.lovaiapi.dto.user.ChangePasswordRequest;
import com.lovai.lovaiapi.dto.user.LoginRequest;
import com.lovai.lovaiapi.dto.user.LoginResponse;
import com.lovai.lovaiapi.dto.user.UserSearchResponse;
import com.lovai.lovaiapi.model.User;
import com.lovai.lovaiapi.model.EmailVerificationToken;
import com.lovai.lovaiapi.repository.UserRepository;
import com.lovai.lovaiapi.repository.EmailVerificationTokenRepository;
import com.lovai.lovaiapi.util.JwtUtil;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       EmailVerificationTokenRepository tokenRepository,
                       EmailService emailService,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
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

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải khác mật khẩu cũ");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng"));

        if (!user.isVerified()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản chưa được xác minh");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        
        return response;
    }

    public UserSearchResponse searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                keyword, keyword, pageable);

        List<UserResponse> userResponses = new ArrayList<>();
        for (User user : userPage.getContent()) {
            UserResponse response = new UserResponse();
            response.setId(user.getId());
            response.setEmail(user.getEmail());
            response.setName(user.getName());
            response.setGender(user.getGender());
            response.setDateOfBirth(user.getDateOfBirth());
            userResponses.add(response);
        }

        UserSearchResponse searchResponse = new UserSearchResponse();
        searchResponse.setUsers(userResponses);
        searchResponse.setTotalPages(userPage.getTotalPages());
        searchResponse.setTotalElements(userPage.getTotalElements());
        searchResponse.setCurrentPage(page);
        searchResponse.setPageSize(size);
        searchResponse.setHasNext(userPage.hasNext());
        searchResponse.setHasPrevious(userPage.hasPrevious());

        return searchResponse;
    }
}
