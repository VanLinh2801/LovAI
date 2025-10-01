package com.lovai.lovaiapi.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserSearchRequest {
    
    @NotBlank(message = "Từ khóa tìm kiếm không được để trống")
    private String keyword;
}
