package com.lovai.lovaiapi.repository;

import com.lovai.lovaiapi.model.DatePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DatePlanRepository extends JpaRepository<DatePlan, UUID> {
    
    @Query("SELECT dp FROM DatePlan dp WHERE dp.couple.id = :coupleId AND dp.deletedAt IS NULL ORDER BY dp.startTime DESC, dp.createdAt DESC")
    List<DatePlan> findByCoupleIdAndNotDeleted(@Param("coupleId") UUID coupleId);
    
    @Query("SELECT dp FROM DatePlan dp WHERE dp.id = :planId AND dp.couple.id = :coupleId AND dp.deletedAt IS NULL")
    Optional<DatePlan> findByIdAndCoupleId(@Param("planId") UUID planId, @Param("coupleId") UUID coupleId);
    
    @Query("SELECT COUNT(dp) FROM DatePlan dp WHERE dp.couple.id = :coupleId AND dp.deletedAt IS NULL")
    long countByCoupleId(@Param("coupleId") UUID coupleId);
    
    @Query("SELECT COUNT(dp) FROM DatePlan dp WHERE dp.couple.id = :coupleId AND dp.deletedAt IS NULL " +
           "AND dp.createdAt >= :startDate AND dp.createdAt <= :endDate")
    long countByCoupleIdAndDateRange(@Param("coupleId") UUID coupleId,
                                    @Param("startDate") java.time.OffsetDateTime startDate,
                                    @Param("endDate") java.time.OffsetDateTime endDate);
}

