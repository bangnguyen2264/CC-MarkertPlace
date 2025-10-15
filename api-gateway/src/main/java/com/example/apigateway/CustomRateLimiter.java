package com.example.apigateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomRateLimiter extends AbstractGatewayFilterFactory<CustomRateLimiter.Config> {

    private final CacheService cacheService;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientId = getClientId(exchange);
            String key = "rate_limit:" + clientId;

            return cacheService.get(key)
                    .defaultIfEmpty(0L)
                    .flatMap(count -> {
                        long currentCount = count instanceof Number ? ((Number) count).longValue() : 0L;

                        if (currentCount >= config.getLimit()) {
                            log.warn("Rate limit exceeded for client: {}", clientId);
                            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                            exchange.getResponse().getHeaders().add("X-Rate-Limit-Retry-After-Seconds",
                                    String.valueOf(config.getTtl().getSeconds()));
                            return exchange.getResponse().setComplete();
                        }

                        return cacheService.increment(key)
                                .flatMap(newCount -> {
                                    if (newCount == 1) {
                                        return cacheService.expire(key, config.getTtl())
                                                .then(chain.filter(exchange));
                                    }
                                    exchange.getResponse().getHeaders().add("X-Rate-Limit-Remaining",
                                            String.valueOf(config.getLimit() - newCount));
                                    return chain.filter(exchange);
                                });
                    });
        };
    }

    private String getClientId(ServerWebExchange exchange) {
        // Try to get user from JWT header first
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Name");
        if (username != null) {
            return username;
        }

        // Fallback to IP address
        String ip = exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
        return ip;
    }

    public static class Config {
        private long limit = 100;
        private Duration ttl = Duration.ofMinutes(1);

        public long getLimit() {
            return limit;
        }

        public void setLimit(long limit) {
            this.limit = limit;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }
}