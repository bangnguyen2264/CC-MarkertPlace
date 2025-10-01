package com.example.userservice.model.dto.response;

import com.example.userservice.model.entity.User;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class AuthResponse implements Serializable {
    private Long id;
    private String token;
    private String refreshToken;
    private String username;
    private String role;

    public static AuthResponse from(User user, String token, String refreshToken) {
        return AuthResponse.builder()
                .id(user.getId())
                .token(token)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .role(user.getRole().toString())
                .build();
    }
}
