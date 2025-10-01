package com.example.userservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LoginRequest {
    @NotBlank
    private String email;
    @NotBlank
    @Size(min = 8)
    private String password;
}
