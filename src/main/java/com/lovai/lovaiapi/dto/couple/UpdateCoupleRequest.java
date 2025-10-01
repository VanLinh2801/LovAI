package com.lovai.lovaiapi.dto.couple;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateCoupleRequest {
    
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    
    @NotNull(message = "Ngày kỷ niệm không được để trống")
    private LocalDate anniversaryDate;
    
    @Valid
    private PartnerInfo partner;
    
    @Data
    public static class PartnerInfo {
        @NotBlank(message = "Tên partner không được để trống")
        private String name;
        
        @NotNull(message = "Giới tính partner không được để trống")
        private com.lovai.lovaiapi.model.enums.Gender gender;
        
        private LocalDate dateOfBirth;
        
        private String phone;
        
        private String email;
        
        private String notes;
    }
}
