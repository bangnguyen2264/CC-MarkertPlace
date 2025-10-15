package com.example.apigateway;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    private static final String ROLE_CLAIM = "role";
    private static final String SUBJECT_CLAIM = "sub";

    private final JwtProperties jwtProperties;

    public boolean validateAccessToken(String token) {
        if (!StringUtils.hasText(token)) {
            log.warn("Access token is empty or null");
            return false;
        }

        try {
            verifyAccessToken(token);
            return true;
        } catch (JWTVerificationException e) {
            log.error("Access token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public DecodedJWT decodeToken(String token) {
        try {
            return JWT.decode(token);
        } catch (Exception e) {
            log.error("Failed to decode token: {}", e.getMessage());
            return null;
        }
    }

    public String extractUsername(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getSubject();
        } catch (Exception e) {
            log.error("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(ROLE_CLAIM).asString();
        } catch (Exception e) {
            log.error("Failed to extract role from token: {}", e.getMessage());
            return null;
        }
    }

    private DecodedJWT verifyAccessToken(String token) {
        return JWT.require(Algorithm.HMAC256(jwtProperties.getAccessSecret()))
                .acceptExpiresAt(jwtProperties.getAccessExpiresAt())
                .withClaimPresence(SUBJECT_CLAIM)
                .withClaimPresence(ROLE_CLAIM)
                .build()
                .verify(token);
    }

    public String extractTokenFromHeader(String authHeader) {
        if (!isValidAuthHeader(authHeader)) {
            log.warn("Invalid or missing Authorization header");
            return null;
        }
        return authHeader.replaceFirst(jwtProperties.getPrefix(), "").trim();
    }

    private boolean isValidAuthHeader(String authHeader) {
        return StringUtils.hasText(authHeader) && authHeader.startsWith(jwtProperties.getPrefix());
    }
}
