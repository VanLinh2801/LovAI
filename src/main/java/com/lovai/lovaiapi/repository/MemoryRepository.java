package com.lovai.lovaiapi.repository;

import com.lovai.lovaiapi.model.Memory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MemoryRepository extends JpaRepository<Memory, UUID> {
    
    @Query("SELECT m FROM Memory m WHERE m.couple.id = :coupleId AND m.deletedAt IS NULL ORDER BY m.happenedAt DESC, m.createdAt DESC")
    List<Memory> findByCoupleIdAndNotDeleted(@Param("coupleId") UUID coupleId);
    
    @Query("SELECT m FROM Memory m WHERE m.id = :memoryId AND m.couple.id = :coupleId AND m.deletedAt IS NULL")
    Memory findByIdAndCoupleId(@Param("memoryId") UUID memoryId, @Param("coupleId") UUID coupleId);
    
    @Query("SELECT m FROM Memory m WHERE m.couple.id = :coupleId AND m.deletedAt IS NULL " +
           "AND m.happenedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY m.happenedAt DESC")
    List<Memory> findByCoupleIdAndDateRange(@Param("coupleId") UUID coupleId, 
                                           @Param("startDate") java.time.OffsetDateTime startDate,
                                           @Param("endDate") java.time.OffsetDateTime endDate);
    
    @Query("SELECT m FROM Memory m WHERE m.couple.id = :coupleId AND m.deletedAt IS NULL " +
           "AND (LOWER(m.title) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(m.description) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
           "ORDER BY m.happenedAt DESC")
    List<Memory> findByCoupleIdAndSearchText(@Param("coupleId") UUID coupleId, 
                                           @Param("searchText") String searchText);
    
    @Query("SELECT COUNT(m) FROM Memory m WHERE m.couple.id = :coupleId AND m.deletedAt IS NULL")
    long countByCoupleId(@Param("coupleId") UUID coupleId);
    
    @Query("SELECT COUNT(m) FROM Memory m WHERE m.couple.id = :coupleId AND m.deletedAt IS NULL " +
           "AND m.happenedAt BETWEEN :startDate AND :endDate")
    long countByCoupleIdAndDateRange(@Param("coupleId") UUID coupleId, 
                                    @Param("startDate") java.time.OffsetDateTime startDate,
                                    @Param("endDate") java.time.OffsetDateTime endDate);
    
    @Query("SELECT COUNT(m) FROM Memory m WHERE m.couple.id = :coupleId AND m.deletedAt IS NULL " +
           "AND LOWER(m.locationText) LIKE LOWER(CONCAT('%', :locationText, '%'))")
    long countByCoupleIdAndLocation(@Param("coupleId") UUID coupleId, 
                                  @Param("locationText") String locationText);
    
    @Query("SELECT m FROM Memory m WHERE m.couple.id = :coupleId AND m.deletedAt IS NULL ORDER BY m.happenedAt DESC, m.createdAt DESC")
    List<Memory> findByCoupleIdWithPagination(@Param("coupleId") UUID coupleId, 
                                            org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT m FROM Memory m WHERE m.couple.id = :coupleId AND m.deletedAt IS NULL " +
           "AND (LOWER(m.title) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(m.description) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
           "ORDER BY m.happenedAt DESC")
    List<Memory> findByCoupleIdAndSearchTextWithPagination(@Param("coupleId") UUID coupleId, 
                                                           @Param("searchText") String searchText,
                                                           org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT COALESCE(SUM(m.mediaCount), 0) FROM Memory m WHERE m.couple.id = :coupleId AND m.deletedAt IS NULL")
    long sumMediaCountByCoupleId(@Param("coupleId") UUID coupleId);
    
    @Query("SELECT COALESCE(SUM(m.mediaCount), 0) FROM Memory m WHERE m.couple.id = :coupleId AND m.deletedAt IS NULL " +
           "AND m.happenedAt BETWEEN :startDate AND :endDate")
    long sumMediaCountByCoupleIdAndDateRange(@Param("coupleId") UUID coupleId,
                                             @Param("startDate") java.time.OffsetDateTime startDate,
                                             @Param("endDate") java.time.OffsetDateTime endDate);
}
