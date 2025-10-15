package com.example.apigateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    // ✅ QUAN TRỌNG: Phải có constructor này
    public JwtAuthenticationFilter(JwtUtil jwtUtil, JwtProperties jwtProperties) {
        super(Config.class);  // ← Dòng này rất quan trọng!
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            log.debug("Processing JWT authentication for path: {}", path);

            // Extract token from header
            String authHeader = request.getHeaders().getFirst(jwtProperties.getAuthHeader());
            if (authHeader == null || authHeader.isEmpty()) {
                log.warn("Missing Authorization header for path: {}", path);
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = jwtUtil.extractTokenFromHeader(authHeader);
            if (token == null) {
                log.warn("Invalid Authorization header format for path: {}", path);
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            // Validate token
            if (!jwtUtil.validateAccessToken(token)) {
                log.warn("Invalid JWT token for path: {}", path);
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            // Extract user info and add to headers
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            if (username == null || role == null) {
                log.warn("Failed to extract user info from token for path: {}", path);
                return onError(exchange, "Invalid token claims", HttpStatus.UNAUTHORIZED);
            }

            log.debug("Successfully authenticated user: {} with role: {} for path: {}", username, role, path);

            // Add user info to request headers for downstream services
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-Name", username)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        String errorJson = String.format("{\"error\": \"%s\", \"status\": %d}", message, status.value());
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(errorJson.getBytes()))
        );
    }

    public static class Config {
        // Configuration properties if needed
        // Có thể thêm fields nếu cần pass config từ YAML
    }
}