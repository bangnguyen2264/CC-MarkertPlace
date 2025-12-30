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

            log.info("➡️ Incoming request: {} {}", method, path);

            SecurityRule rule = ruleMatcher.match(path, request.getMethod());

            if (rule == null) {
                log.warn("⚠️ No security rule matched for {} {}", method, path);
            } else {
                log.info("✅ Matched rule:");
                log.info("   - rule.path     = {}", rule.getPath());
                log.info("   - rule.methods  = {}", rule.getMethods());
                log.info("   - rule.access   = {}", rule.getAccess());
            }

            // 🔓 PUBLIC check
            if (rule == null) {
                log.warn("⚠️ No rule matched → allow request by default");
                return chain.filter(exchange);
            }

            if (isPublicAccess(rule)) {
                log.info("🔓 PUBLIC access granted");
                return chain.filter(exchange);
            }

            log.info("🛡️ Authentication required for {} {}", method, path);

            // 🧾 Header check
            String authHeader = request.getHeaders().getFirst(jwtProperties.getAuthHeader());
            log.debug("Authorization header: {}", authHeader);

            if (authHeader == null || authHeader.isEmpty()) {
                log.warn("❌ Missing Authorization header");
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = jwtUtil.extractTokenFromHeader(authHeader);
            log.debug("Extracted token: {}", token);

            if (token == null || !jwtUtil.validateAccessToken(token)) {
                log.warn("Invalid or expired token");
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            log.info("Authenticated user: username={}, role={}", username, role);

            // ⚖️ Role check
            if (rule != null && !hasRequiredRole(rule, role)) {
                log.warn("Forbidden: role {} not allowed, required={}", role, rule.getAccess());
                return onError(exchange, "Forbidden: insufficient permissions", HttpStatus.FORBIDDEN);
            }

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Name", username)
                    .header("X-User-Role", role)
                    .build();

            log.info("✅ Request authorized, forwarding...");
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }


    private boolean isPublicAccess(SecurityRule rule) {
        return rule.getAccess() != null &&
                rule.getAccess().stream()
                        .map(String::trim)
                        .anyMatch(r -> r.equalsIgnoreCase("PUBLIC"));
    }


    private boolean hasRequiredRole(SecurityRule rule, String userRole) {
        if (rule.getAccess() == null || rule.getAccess().isEmpty()) {
            return true; // Nếu không định nghĩa role → cho phép tất cả
        }

        // Nếu có PUBLIC → cho phép tất cả
        if (isPublicAccess(rule)) {
            return true;
        }

        // Kiểm tra role cụ thể
        return rule.getAccess().stream()
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