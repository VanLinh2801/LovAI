package com.lovai.lovaiapi.service;

import com.lovai.lovaiapi.model.PushToken;
import com.lovai.lovaiapi.model.User;
import com.lovai.lovaiapi.model.enums.Platform;
import com.lovai.lovaiapi.repository.PushTokenRepository;
import com.lovai.lovaiapi.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PushTokenService {

    private static final Logger logger = LoggerFactory.getLogger(PushTokenService.class);

    private final PushTokenRepository pushTokenRepository;
    private final UserRepository userRepository;

    public PushTokenService(PushTokenRepository pushTokenRepository, UserRepository userRepository) {
        this.pushTokenRepository = pushTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PushToken createOrUpdateToken(UUID userId, String token, Platform platform, String deviceId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            Optional<PushToken> existingToken = pushTokenRepository.findByToken(token);
            
            if (existingToken.isPresent()) {
                PushToken pushToken = existingToken.get();
                pushToken.setUser(user);
                pushToken.setPlatform(platform);
                pushToken.setDeviceId(deviceId);
                pushToken.setActive(true);
                pushToken.setLastSeenAt(OffsetDateTime.now());
                
                pushToken = pushTokenRepository.save(pushToken);
                logger.info("Updated push token for user {}: {}", userId, token);
                return pushToken;
            } else {
                PushToken pushToken = new PushToken();
                pushToken.setUser(user);
                pushToken.setToken(token);
                pushToken.setPlatform(platform);
                pushToken.setDeviceId(deviceId);
                pushToken.setActive(true);
                pushToken.setLastSeenAt(OffsetDateTime.now());
                
                pushToken = pushTokenRepository.save(pushToken);
                logger.info("Created new push token for user {}: {}", userId, token);
                return pushToken;
            }
            
        } catch (Exception e) {
            logger.error("Error creating/updating push token for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void deactivateToken(String token) {
        try {
            Optional<PushToken> pushTokenOpt = pushTokenRepository.findByToken(token);
            
            if (pushTokenOpt.isPresent()) {
                PushToken pushToken = pushTokenOpt.get();
                pushToken.setActive(false);
                pushTokenRepository.save(pushToken);
                
                logger.info("Deactivated push token: {}", token);
            } else {
                logger.warn("Push token not found for deactivation: {}", token);
            }
            
        } catch (Exception e) {
            logger.error("Error deactivating push token {}: {}", token, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void updateLastSeenAt(UUID userId) {
        try {
            List<PushToken> activeTokens = pushTokenRepository.findByUserIdAndActiveTrue(userId);
            
            OffsetDateTime now = OffsetDateTime.now();
            for (PushToken token : activeTokens) {
                token.setLastSeenAt(now);
            }
            
            if (!activeTokens.isEmpty()) {
                pushTokenRepository.saveAll(activeTokens);
                logger.debug("Updated lastSeenAt for {} push tokens of user {}", activeTokens.size(), userId);
            }
            
        } catch (Exception e) {
            logger.error("Error updating lastSeenAt for user {}: {}", userId, e.getMessage(), e);
        }
    }

    public List<PushToken> getActiveTokensByUserId(UUID userId) {
        return pushTokenRepository.findByUserIdAndActiveTrue(userId);
    }

    public List<PushToken> getTokensByUserIdAndPlatform(UUID userId, Platform platform) {
        return pushTokenRepository.findByUserIdAndPlatformAndActiveTrue(userId, platform);
    }

    public long countActiveTokensByUserId(UUID userId) {
        return pushTokenRepository.countByUserIdAndActiveTrue(userId);
    }

    @Transactional
    public int cleanupInactiveTokens(OffsetDateTime cutoffDate) {
        try {
            List<PushToken> inactiveTokens = pushTokenRepository.findInactiveTokensOlderThan(cutoffDate);
            
            if (!inactiveTokens.isEmpty()) {
                pushTokenRepository.deleteAll(inactiveTokens);
                logger.info("Cleaned up {} inactive push tokens", inactiveTokens.size());
            }
            
            return inactiveTokens.size();
            
        } catch (Exception e) {
            logger.error("Error cleaning up inactive tokens: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Transactional
    public void deactivateAllTokensByUserId(UUID userId) {
        try {
            List<PushToken> activeTokens = pushTokenRepository.findByUserIdAndActiveTrue(userId);
            
            for (PushToken token : activeTokens) {
                token.setActive(false);
            }
            
            if (!activeTokens.isEmpty()) {
                pushTokenRepository.saveAll(activeTokens);
                logger.info("Deactivated {} push tokens for user {}", activeTokens.size(), userId);
            }
            
        } catch (Exception e) {
            logger.error("Error deactivating all tokens for user {}: {}", userId, e.getMessage(), e);
        }
    }
}
