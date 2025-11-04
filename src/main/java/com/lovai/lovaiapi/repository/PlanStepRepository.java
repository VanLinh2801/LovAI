package com.lovai.lovaiapi.repository;

import com.lovai.lovaiapi.model.PlanStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlanStepRepository extends JpaRepository<PlanStep, UUID> {
    
    @Query("SELECT ps FROM PlanStep ps WHERE ps.plan.id = :planId ORDER BY ps.stepOrder ASC")
    List<PlanStep> findByPlanIdOrderByStepOrder(@Param("planId") UUID planId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM PlanStep ps WHERE ps.plan.id = :planId")
    void deleteByPlanId(@Param("planId") UUID planId);
}

