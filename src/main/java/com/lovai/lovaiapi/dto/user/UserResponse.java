package com.lovai.lovaiapi.dto.user;

import java.time.LocalDate;
import java.util.UUID;
import com.lovai.lovaiapi.model.enums.Gender;
import lombok.Data;

@Data
public class UserResponse {
    private UUID id;
    private String email;
    private String name;
    private Gender gender;
    private LocalDate dateOfBirth;
}
