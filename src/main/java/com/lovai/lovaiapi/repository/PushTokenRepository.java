package com.lovai.lovaiapi.repository;

import com.lovai.lovaiapi.model.PushToken;
import com.lovai.lovaiapi.model.enums.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PushTokenRepository extends JpaRepository<PushToken, UUID> {
    
    List<PushToken> findByUserIdAndActiveTrue(UUID userId);
    
    Optional<PushToken> findByToken(String token);
    
    @Query("SELECT pt FROM PushToken pt WHERE pt.user.id = :userId AND pt.platform = :platform AND pt.active = true")
    List<PushToken> findByUserIdAndPlatformAndActiveTrue(@Param("userId") UUID userId, 
                                                         @Param("platform") Platform platform);
    
    @Query("SELECT pt FROM PushToken pt WHERE pt.deviceId = :deviceId AND pt.active = true")
    List<PushToken> findByDeviceIdAndActiveTrue(@Param("deviceId") String deviceId);
    
    @Query("SELECT COUNT(pt) FROM PushToken pt WHERE pt.user.id = :userId AND pt.active = true")
    long countByUserIdAndActiveTrue(@Param("userId") UUID userId);
    
    @Query("SELECT pt FROM PushToken pt WHERE pt.active = false AND pt.lastSeenAt < :cutoffDate")
    List<PushToken> findInactiveTokensOlderThan(@Param("cutoffDate") java.time.OffsetDateTime cutoffDate);
}

