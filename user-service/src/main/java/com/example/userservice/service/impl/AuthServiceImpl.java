package com.example.userservice.service.impl;

import com.example.commondto.exception.ConflictException;
import com.example.commondto.exception.UnauthorizedException;
import com.example.userservice.integration.WalletIntegrationService;
import com.example.userservice.model.dto.request.LoginRequest;
import com.example.userservice.model.dto.request.RegisterRequest;
import com.example.userservice.model.dto.response.AuthResponse;
import com.example.userservice.model.entity.User;
import com.example.userservice.model.security.JwtService;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final WalletIntegrationService walletIntegrationService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        String accessToken = jwtService.generateAccessToken(authentication);
        String refreshToken = jwtService.generateRefreshToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found with username: " + loginRequest.getEmail()));

        log.info("User {} logged in", user.getUsername());
        return AuthResponse.from(user, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public String register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("Email already exists");
        }


        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .dob(registerRequest.getDob())
                .build();

        userRepository.save(user);
        walletIntegrationService.createWalletForUser(user.getId());
        log.info("User {} registered", user.getUsername());
        return "Success register new user";
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        try {
            if (refreshToken != null) {
                if (jwtService.validateRefreshToken(refreshToken)) {
                    Authentication auth = jwtService.createAuthentication(refreshToken);

                    User user = userRepository.findByEmail(auth.getName())
                            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

                    log.info("User {} refreshed token", user.getUsername());
                    return AuthResponse.from(user, jwtService.generateAccessToken(auth), refreshToken);
                }
            }
            throw new UnauthorizedException("Invalid refresh token");
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid refresh token");
        }
    }


}
