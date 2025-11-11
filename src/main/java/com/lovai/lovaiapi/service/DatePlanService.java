package com.lovai.lovaiapi.service;

import com.lovai.lovaiapi.dto.plan.DatePlanCreateRequest;
import com.lovai.lovaiapi.dto.plan.DatePlanResponse;
import com.lovai.lovaiapi.dto.plan.DatePlanUpdateRequest;
import com.lovai.lovaiapi.dto.plan.PlanStepCreateRequest;
import com.lovai.lovaiapi.dto.plan.PlanStepResponse;
import com.lovai.lovaiapi.dto.plan.PlanStepUpdateRequest;
import com.lovai.lovaiapi.exception.NotFoundException;
import com.lovai.lovaiapi.model.Couple;
import com.lovai.lovaiapi.model.DatePlan;
import com.lovai.lovaiapi.model.PlanStep;
import com.lovai.lovaiapi.model.enums.PlanStatus;
import com.lovai.lovaiapi.model.ExternalVenue;
import com.lovai.lovaiapi.repository.CoupleRepository;
import com.lovai.lovaiapi.repository.DatePlanRepository;
import com.lovai.lovaiapi.repository.ExternalVenueRepository;
import com.lovai.lovaiapi.repository.PlanStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DatePlanService {
    
    @Autowired
    private DatePlanRepository datePlanRepository;
    
    @Autowired
    private PlanStepRepository planStepRepository;
    
    @Autowired
    private CoupleRepository coupleRepository;
    
    @Autowired
    private ExternalVenueRepository externalVenueRepository;
    
    public DatePlanResponse createDatePlan(DatePlanCreateRequest request) {
        Couple couple = coupleRepository.findById(request.getCoupleId())
                .orElseThrow(() -> new NotFoundException("Couple not found"));
        
        DatePlan datePlan = DatePlan.builder()
                .couple(couple)
                .title(request.getTitle())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(request.getStatus() != null ? request.getStatus() : PlanStatus.planned)
                .note(request.getNote())
                .budgetEstimate(request.getBudgetEstimate())
                .weatherSnapshot(request.getWeatherSnapshot())
                .meta(request.getMeta() != null ? request.getMeta() : Map.of())
                .build();
        
        DatePlan savedPlan = datePlanRepository.save(datePlan);
        
        if (request.getPlanSteps() != null && !request.getPlanSteps().isEmpty()) {
            List<PlanStep> planSteps = new ArrayList<>();
            for (PlanStepCreateRequest stepRequest : request.getPlanSteps()) {
                ExternalVenue place = null;
                if (stepRequest.getPlaceId() != null) {
                    place = externalVenueRepository.findById(stepRequest.getPlaceId())
                            .orElse(null); 
                }
                
                PlanStep planStep = PlanStep.builder()
                        .plan(savedPlan)
                        .place(place)
                        .stepOrder(stepRequest.getStepOrder())
                        .actionType(stepRequest.getActionType())
                        .placeSnapshot(stepRequest.getPlaceSnapshot() != null ? stepRequest.getPlaceSnapshot() : Map.of())
                        .note(stepRequest.getNote())
                        .build();
                planSteps.add(planStep);
            }
            planStepRepository.saveAll(planSteps);
        }
        
        return convertToResponse(savedPlan);
    }
    
    @Transactional(readOnly = true)
    public DatePlanResponse getDatePlanById(UUID planId, UUID coupleId) {
        DatePlan datePlan = datePlanRepository.findByIdAndCoupleId(planId, coupleId)
                .orElseThrow(() -> new NotFoundException("Date plan not found"));
        
        return convertToResponse(datePlan);
    }
    
    @Transactional(readOnly = true)
    public List<DatePlanResponse> getDatePlansByCoupleId(UUID coupleId) {
        List<DatePlan> datePlans = datePlanRepository.findByCoupleIdAndNotDeleted(coupleId);
        return datePlans.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public DatePlanResponse updateDatePlan(UUID planId, UUID coupleId, DatePlanUpdateRequest request) {
        DatePlan datePlan = datePlanRepository.findByIdAndCoupleId(planId, coupleId)
                .orElseThrow(() -> new NotFoundException("Date plan not found"));
        
        LocalDateTime newStartTime = request.getStartTime() != null ? request.getStartTime() : datePlan.getStartTime();
        LocalDateTime newEndTime = request.getEndTime() != null ? request.getEndTime() : datePlan.getEndTime();
        
        if (request.getStartTime() != null && newStartTime.isBefore(LocalDateTime.now())) {
            throw new com.lovai.lovaiapi.exception.BadRequestException("Start time must be in the future");
        }
        
        if (newStartTime != null && newEndTime != null && !newEndTime.isAfter(newStartTime)) {
            throw new com.lovai.lovaiapi.exception.BadRequestException("End time must be after start time");
        }
        
        if (request.getTitle() != null) {
            datePlan.setTitle(request.getTitle());
        }
        if (request.getStartTime() != null) {
            datePlan.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            datePlan.setEndTime(request.getEndTime());
        }
        if (request.getStatus() != null) {
            datePlan.setStatus(request.getStatus());
        }
        if (request.getNote() != null) {
            datePlan.setNote(request.getNote());
        }
        if (request.getBudgetEstimate() != null) {
            datePlan.setBudgetEstimate(request.getBudgetEstimate());
        }
        if (request.getWeatherSnapshot() != null) {
            datePlan.setWeatherSnapshot(request.getWeatherSnapshot());
        }
        if (request.getMeta() != null) {
            datePlan.setMeta(request.getMeta());
        }
        
        DatePlan updatedPlan = datePlanRepository.save(datePlan);
        
        if (request.getPlanSteps() != null) {
            planStepRepository.deleteByPlanId(planId);
            
            if (!request.getPlanSteps().isEmpty()) {
                List<PlanStep> planSteps = new ArrayList<>();
                for (PlanStepUpdateRequest stepRequest : request.getPlanSteps()) {
                    ExternalVenue place = null;
                    if (stepRequest.getPlaceId() != null) {
                        place = externalVenueRepository.findById(stepRequest.getPlaceId())
                                .orElse(null); 
                    }
                    
                    PlanStep planStep = PlanStep.builder()
                            .plan(updatedPlan)
                            .place(place)
                            .stepOrder(stepRequest.getStepOrder() != null ? stepRequest.getStepOrder() : 0)
                            .actionType(stepRequest.getActionType())
                            .placeSnapshot(stepRequest.getPlaceSnapshot() != null ? stepRequest.getPlaceSnapshot() : Map.of())
                            .note(stepRequest.getNote())
                            .build();
                    planSteps.add(planStep);
                }
                planStepRepository.saveAll(planSteps);
            }
        }
        
        return convertToResponse(updatedPlan);
    }
    
    public void deleteDatePlan(UUID planId, UUID coupleId) {
        DatePlan datePlan = datePlanRepository.findByIdAndCoupleId(planId, coupleId)
                .orElseThrow(() -> new NotFoundException("Date plan not found"));

        planStepRepository.deleteByPlanId(planId);

        datePlan.setDeletedAt(java.time.OffsetDateTime.now());
        datePlanRepository.save(datePlan);
    }
    
    public PlanStepResponse addStepToDatePlan(UUID planId, UUID coupleId, PlanStepCreateRequest request) {
        DatePlan datePlan = datePlanRepository.findByIdAndCoupleId(planId, coupleId)
                .orElseThrow(() -> new NotFoundException("Date plan not found"));
        
        ExternalVenue place = null;
        if (request.getPlaceId() != null) {
            place = externalVenueRepository.findById(request.getPlaceId())
                    .orElse(null); 
        }
        
        PlanStep planStep = PlanStep.builder()
                .plan(datePlan)
                .place(place)
                .stepOrder(request.getStepOrder())
                .actionType(request.getActionType())
                .placeSnapshot(request.getPlaceSnapshot() != null ? request.getPlaceSnapshot() : Map.of())
                .note(request.getNote())
                .build();
        
        PlanStep savedStep = planStepRepository.save(planStep);
        return convertPlanStepToResponse(savedStep);
    }
    
    public void deleteStepFromDatePlan(UUID planId, UUID coupleId, UUID stepId) {
        datePlanRepository.findByIdAndCoupleId(planId, coupleId)
                .orElseThrow(() -> new NotFoundException("Date plan not found"));
        
        PlanStep planStep = planStepRepository.findById(stepId)
                .orElseThrow(() -> new NotFoundException("Plan step not found"));
        
        if (!planStep.getPlan().getId().equals(planId)) {
            throw new NotFoundException("Plan step does not belong to this date plan");
        }
        
        planStepRepository.delete(planStep);
    }
    
    private DatePlanResponse convertToResponse(DatePlan datePlan) {
        List<PlanStep> planSteps = planStepRepository.findByPlanIdOrderByStepOrder(datePlan.getId());
        List<PlanStepResponse> planStepResponses = planSteps.stream()
                .map(this::convertPlanStepToResponse)
                .collect(Collectors.toList());
        
        return DatePlanResponse.builder()
                .id(datePlan.getId())
                .coupleId(datePlan.getCouple().getId())
                .title(datePlan.getTitle())
                .startTime(datePlan.getStartTime())
                .endTime(datePlan.getEndTime())
                .status(datePlan.getStatus())
                .note(datePlan.getNote())
                .budgetEstimate(datePlan.getBudgetEstimate())
                .weatherSnapshot(datePlan.getWeatherSnapshot())
                .meta(datePlan.getMeta())
                .createdAt(datePlan.getCreatedAt())
                .updatedAt(datePlan.getUpdatedAt())
                .planSteps(planStepResponses)
                .build();
    }
    
    private PlanStepResponse convertPlanStepToResponse(PlanStep planStep) {
        return PlanStepResponse.builder()
                .id(planStep.getId())
                .planId(planStep.getPlan().getId())
                .stepOrder(planStep.getStepOrder())
                .actionType(planStep.getActionType())
                .placeSnapshot(planStep.getPlaceSnapshot())
                .placeId(planStep.getPlace() != null ? planStep.getPlace().getId() : null)
                .note(planStep.getNote())
                .build();
    }
}

