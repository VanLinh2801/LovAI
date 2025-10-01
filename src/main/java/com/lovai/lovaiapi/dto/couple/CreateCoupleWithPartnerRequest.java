package com.lovai.lovaiapi.dto.couple;

import com.lovai.lovaiapi.model.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateCoupleWithPartnerRequest {
    
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    
    @NotNull(message = "Ngày kỷ niệm không được để trống")
    private LocalDate anniversaryDate;
    
    @NotNull(message = "User ID không được để trống")
    private UUID userId;
    
    @Valid
    @NotNull(message = "Thông tin partner không được để trống")
    private PartnerInfo partner;
    
    @Data
    public static class PartnerInfo {
        @NotBlank(message = "Tên partner không được để trống")
        private String name;
        
        @NotNull(message = "Giới tính partner không được để trống")
        private Gender gender;
        
        private LocalDate dateOfBirth;
        
        private String phone;
        
        private String email;
        
        private String notes;
    }
}
