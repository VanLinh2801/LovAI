package com.lovai.lovaiapi.repository;

import com.lovai.lovaiapi.model.Couple;
import com.lovai.lovaiapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoupleRepository extends JpaRepository<Couple, UUID> {
    
    @Query("SELECT c FROM Couple c WHERE (c.user1 = :user1 OR c.user2 = :user2) AND c.deletedAt IS NULL")
    List<Couple> findByUser1OrUser2AndDeletedAtIsNull(@Param("user1") User user1, @Param("user2") User user2);
    
    @Query("SELECT COUNT(c) > 0 FROM Couple c WHERE (c.user1 = :user1 OR c.user2 = :user2) AND c.deletedAt IS NULL")
    boolean existsByUser1OrUser2AndDeletedAtIsNull(@Param("user1") User user1, @Param("user2") User user2);
    
    @Query("SELECT c FROM Couple c WHERE c.user1 = :user1 AND c.deletedAt IS NULL")
    Optional<Couple> findByUser1AndDeletedAtIsNull(@Param("user1") User user1);
    
    @Query("SELECT c FROM Couple c WHERE c.user2 = :user2 AND c.deletedAt IS NULL")
    Optional<Couple> findByUser2AndDeletedAtIsNull(@Param("user2") User user2);
    
    @Query("SELECT c FROM Couple c WHERE ((c.user1 = :user1 AND c.user2 = :user2) OR (c.user1 = :user2 AND c.user2 = :user1)) AND c.deletedAt IS NOT NULL ORDER BY c.deletedAt DESC")
    List<Couple> findByUser1AndUser2AndDeletedAtIsNotNull(@Param("user1") User user1, @Param("user2") User user2);
}
