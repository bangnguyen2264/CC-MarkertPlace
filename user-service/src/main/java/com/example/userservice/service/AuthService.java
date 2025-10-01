package com.example.userservice.service;

import com.example.userservice.model.dto.request.LoginRequest;
import com.example.userservice.model.dto.request.RegisterRequest;
import com.example.userservice.model.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);
    String register(RegisterRequest registerRequest);
    AuthResponse refreshToken(String refreshToken);
}
