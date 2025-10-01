package com.lovai.lovaiapi.repository;

import com.lovai.lovaiapi.model.CoupleInvite;
import com.lovai.lovaiapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CoupleInviteRepository extends JpaRepository<CoupleInvite, UUID> {
    
    @Query("SELECT ci FROM CoupleInvite ci WHERE " +
           "((ci.inviter = :user1 AND ci.invitee = :user2) OR (ci.inviter = :user2 AND ci.invitee = :user1)) " +
           "AND (ci.expiresAt IS NULL OR ci.expiresAt > :now) " +
           "AND ci.respondedAt IS NULL")
    List<CoupleInvite> findActiveInvitesBetweenUsers(@Param("user1") User user1, @Param("user2") User user2, @Param("now") OffsetDateTime now);
    
    @Query("SELECT ci FROM CoupleInvite ci WHERE ci.invitee = :user " +
           "AND (ci.expiresAt IS NULL OR ci.expiresAt > :now) " +
           "AND ci.respondedAt IS NULL")
    List<CoupleInvite> findActiveInvitesForUser(@Param("user") User user, @Param("now") OffsetDateTime now);
    
    @Query("SELECT ci FROM CoupleInvite ci WHERE ci.inviter = :user " +
           "AND (ci.expiresAt IS NULL OR ci.expiresAt > :now) " +
           "AND ci.respondedAt IS NULL")
    List<CoupleInvite> findActiveInvitesSentByUser(@Param("user") User user, @Param("now") OffsetDateTime now);
    
    @Query("SELECT COUNT(ci) > 0 FROM CoupleInvite ci WHERE " +
           "((ci.inviter = :user1 AND ci.invitee = :user2) OR (ci.inviter = :user2 AND ci.invitee = :user1)) " +
           "AND (ci.expiresAt IS NULL OR ci.expiresAt > :now) " +
           "AND ci.respondedAt IS NULL")
    boolean existsActiveInviteBetweenUsers(@Param("user1") User user1, @Param("user2") User user2, @Param("now") OffsetDateTime now);
}
