package com.lovai.lovaiapi.repository;

import com.lovai.lovaiapi.model.EmailVerificationToken;
import com.lovai.lovaiapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findTopByUserAndOtpCodeOrderByCreatedAtDesc(User user, String otpCode);
    Optional<EmailVerificationToken> findTopByUserOrderByCreatedAtDesc(User user);
}
