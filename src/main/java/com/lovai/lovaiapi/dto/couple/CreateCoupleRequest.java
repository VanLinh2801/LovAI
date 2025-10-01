package com.lovai.lovaiapi.dto.couple;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateCoupleRequest {
    
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    
    @NotNull(message = "Ngày kỷ niệm không được để trống")
    private LocalDate anniversaryDate;
    
    @NotNull(message = "User 1 ID không được để trống")
    private UUID user1Id;
    
    @NotNull(message = "User 2 ID không được để trống")
    private UUID user2Id;
}
