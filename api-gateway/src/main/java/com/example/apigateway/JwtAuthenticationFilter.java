package com.example.apigateway;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
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

    // ✅ BẮT BUỘC phải có super(Config.class)
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

            SecurityRule rule = ruleMatcher.match(path, request.getMethod());

            // 🔓 Nếu là PUBLIC → bỏ qua xác thực
            if (rule != null && "PUBLIC".equalsIgnoreCase(rule.getAccess())) {
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
            if (rule != null && !rule.getAccess().contains(role)) {
                return onError(exchange, "Forbidden: role not allowed", HttpStatus.FORBIDDEN);
            }

            ServerHttpRequest modifiedRequest = request.mutate()
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

    // ✅ Bắt buộc phải có inner static class Config
    public static class Config {
        // có thể để trống, nhưng phải có public no-args constructor
        public Config() {
        }
    }
}
