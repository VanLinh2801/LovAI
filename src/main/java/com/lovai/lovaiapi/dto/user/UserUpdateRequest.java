package com.lovai.lovaiapi.dto.user;

import com.lovai.lovaiapi.model.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserUpdateRequest {
    
    @NotBlank(message = "Tên không được để trống")
    private String name;
    
    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;
    
    @NotNull(message = "Giới tính không được để trống")
    private Gender gender;
    
    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate dateOfBirth;
}
