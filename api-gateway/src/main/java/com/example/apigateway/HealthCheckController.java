package com.example.apigateway;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/actuator")
@RequiredArgsConstructor
public class HealthCheckController {

    private final CacheService cacheService;

    @GetMapping("/health/custom")
    public Mono<ResponseEntity<Map<String, Object>>> customHealthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "API Gateway");

        // Check Redis connection
        return cacheService.set("health:check", "ok", java.time.Duration.ofSeconds(10))
                .flatMap(success -> {
                    if (success) {
                        health.put("redis", "UP");
                    } else {
                        health.put("redis", "DOWN");
                        health.put("status", "DEGRADED");
                    }
                    return Mono.just(ResponseEntity.ok(health));
                })
                .onErrorResume(error -> {
                    health.put("redis", "DOWN");
                    health.put("status", "DEGRADED");
                    health.put("error", error.getMessage());
                    return Mono.just(ResponseEntity.status(503).body(health));
                });
    }
}