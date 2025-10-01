package com.example.userservice.model.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.userservice.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private static final String ROLE_CLAIM = "role";
    private static final String SUBJECT_CLAIM = "sub";
    private static final String ISSUED_AT_CLAIM = "iat";

    private final JwtProperties jwtProperties;
    private final UserDetailsServiceImpl userDetailsService;

    public String generateAccessToken(Authentication authentication) {
        validateAuthentication(authentication);

        String role = extractRole(authentication);
        return JWT.create()
                .withSubject(authentication.getName())
                .withClaim(ROLE_CLAIM, role)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusMillis(jwtProperties.getAccessExpiresAt()))
                .sign(Algorithm.HMAC256(jwtProperties.getAccessSecret()));
    }

    public String generateRefreshToken(Authentication authentication) {
        validateAuthentication(authentication);

        return JWT.create()
                .withSubject(authentication.getName())
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusMillis(jwtProperties.getRefreshExpiresAt()))
                .sign(Algorithm.HMAC256(jwtProperties.getRefreshSecret()));
    }

    public boolean validateAccessToken(String token) {
        if (!StringUtils.hasText(token)) {
            log.warn("Access token is empty or null");
            return false;
        }

        try {
            DecodedJWT jwt = verifyAccessToken(token);
            return validateUserAndRole(jwt);
        } catch (JWTVerificationException e) {
            log.error("Access token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        if (!StringUtils.hasText(token)) {
            log.warn("Refresh token is empty or null");
            return false;
        }

        try {
            DecodedJWT jwt = verifyRefreshToken(token);
            return validateUserEnabled(jwt.getSubject());
        } catch (JWTVerificationException e) {
            log.error("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(jwtProperties.getAuthHeader());
        if (!isValidAuthHeader(authHeader)) {
            log.warn("Invalid or missing Authorization header: {}", authHeader);
            return null;
        }
        return authHeader.replaceFirst(jwtProperties.getPrefix(), "").trim();
    }

    public Authentication createAuthentication(String token) {
        if (!StringUtils.hasText(token)) {
            log.warn("Cannot create authentication: token is empty or null");
            return null;
        }

        try {
            DecodedJWT jwt = JWT.decode(token);
            String username = jwt.getSubject();
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return new UsernamePasswordAuthenticationToken(
                    userDetails.getUsername(),
                    null,
                    userDetails.getAuthorities()
            );
        } catch (Exception e) {
            log.error("Failed to create authentication from token: {}", e.getMessage());
            return null;
        }
    }

    private void validateAuthentication(Authentication authentication) {
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            throw new IllegalArgumentException("Authentication object is null or has no principal");
        }
    }

    private String extractRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No authorities found"));
    }

    private DecodedJWT verifyAccessToken(String token) {
        return JWT.require(Algorithm.HMAC256(jwtProperties.getAccessSecret()))
                .acceptExpiresAt(jwtProperties.getAccessExpiresAt())
                .withClaimPresence(SUBJECT_CLAIM)
                .withClaimPresence(ROLE_CLAIM)
                .build()
                .verify(token);
    }

    private DecodedJWT verifyRefreshToken(String token) {
        return JWT.require(Algorithm.HMAC256(jwtProperties.getRefreshSecret()))
                .acceptExpiresAt(jwtProperties.getRefreshExpiresAt())
                .withClaimPresence(SUBJECT_CLAIM)
                .withClaimPresence(ISSUED_AT_CLAIM)
                .build()
                .verify(token);
    }

    private boolean validateUserAndRole(DecodedJWT jwt) {
        String username = jwt.getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!userDetails.isEnabled()) {
            log.warn("Access token validation failed: User {} is disabled", username);
            return false;
        }

        String tokenRole = jwt.getClaim(ROLE_CLAIM).asString();
        String userRole = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");

        if (!tokenRole.equals(userRole)) {
            log.warn("Access token validation failed: Role mismatch, token: {}, user: {}", tokenRole, userRole);
            return false;
        }

        return true;
    }

    private boolean validateUserEnabled(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!userDetails.isEnabled()) {
            log.warn("Refresh token validation failed: User {} is disabled", username);
            return false;
        }
        return true;
    }

    private boolean isValidAuthHeader(String authHeader) {
        return StringUtils.hasText(authHeader) && authHeader.startsWith(jwtProperties.getPrefix());
    }
}