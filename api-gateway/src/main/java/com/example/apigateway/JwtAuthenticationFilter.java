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
    private final SecurityRuleMatcher ruleMatcher;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, JwtProperties jwtProperties, SecurityRuleMatcher ruleMatcher) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
        this.ruleMatcher = ruleMatcher;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
            log.info("Request URI: " + request.getURI());
            SecurityRule rule = ruleMatcher.match(path, request.getMethod());

            // 🔓 Nếu là PUBLIC → bỏ qua xác thực
            if (rule != null && isPublicAccess(rule)) {
                log.debug("Public endpoint: {} {}", method, path);
                return chain.filter(exchange);
            }

            // 🛡️ Nếu yêu cầu xác thực
            String authHeader = request.getHeaders().getFirst(jwtProperties.getAuthHeader());
            if (authHeader == null || authHeader.isEmpty()) {
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = jwtUtil.extractTokenFromHeader(authHeader);
            if (token == null || !jwtUtil.validateAccessToken(token)) {
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            // ⚖️ Kiểm tra role
            if (rule != null && !hasRequiredRole(rule, role)) {
                return onError(exchange, "Forbidden: insufficient permissions", HttpStatus.FORBIDDEN);
            }

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Name", username)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private boolean isPublicAccess(SecurityRule rule) {
        return rule.getRoles() != null &&
                rule.getRoles().stream().anyMatch("PUBLIC"::equalsIgnoreCase);
    }

    private boolean hasRequiredRole(SecurityRule rule, String userRole) {
        if (rule.getRoles() == null || rule.getRoles().isEmpty()) {
            return true; // Nếu không định nghĩa role → cho phép tất cả
        }

        // Nếu có PUBLIC → cho phép tất cả
        if (isPublicAccess(rule)) {
            return true;
        }

        // Kiểm tra role cụ thể
        return rule.getRoles().stream()
                .anyMatch(r -> r.equalsIgnoreCase(userRole));
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
        public Config() {
        }
    }
}