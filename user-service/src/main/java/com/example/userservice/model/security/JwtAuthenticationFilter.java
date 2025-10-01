package com.example.userservice.model.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> PUBLIC_PATTERNS = Arrays.asList(
            "/api/auth/**",
            "/api/users/v3/api-docs/**",
            "/api/users/v3/api-docs",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/**"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Log để debug
        log.info("=== JWT Filter Check ===");
        log.info("Method: {}", method);
        log.info("Path: {}", path);
        log.info("Query: {}", request.getQueryString());

        boolean isPublic = PUBLIC_PATTERNS.stream()
                .anyMatch(pattern -> {
                    boolean matches = pathMatcher.match(pattern, path);
                    log.info("Pattern '{}' matches '{}': {}", pattern, path, matches);
                    return matches;
                });

        log.info("Is public path: {}", isPublic);
        log.info("========================");

        return isPublic;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        log.warn("JWT Filter is processing path: {}", request.getRequestURI());

        try {
            String token = jwtService.extractToken(request);

            if (token == null) {
                log.warn("Missing Authorization header for path: {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Missing Authorization header\"}");
                return;
            }

            if (!jwtService.validateAccessToken(token)) {
                log.warn("Invalid JWT token for path: {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid Authorization token\"}");
                return;
            }

            Authentication auth = jwtService.createAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("Successfully authenticated user for path: {}", request.getRequestURI());

        } catch (Exception e) {
            log.error("Cannot set user authentication for path: {}", request.getRequestURI(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authentication failed\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}