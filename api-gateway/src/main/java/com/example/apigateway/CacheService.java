package com.example.apigateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public Mono<Boolean> set(String key, Object value, Duration ttl) {
        return redisTemplate.opsForValue()
                .set(key, value, ttl)
                .doOnSuccess(result -> log.debug("Cached value for key: {} with TTL: {}", key, ttl))
                .doOnError(error -> log.error("Failed to cache value for key: {}", key, error));
    }

    public Mono<Object> get(String key) {
        return redisTemplate.opsForValue()
                .get(key)
                .doOnSuccess(value -> {
                    if (value != null) {
                        log.debug("Cache hit for key: {}", key);
                    } else {
                        log.debug("Cache miss for key: {}", key);
                    }
                })
                .doOnError(error -> log.error("Failed to get value for key: {}", key, error));
    }

    public Mono<Boolean> delete(String key) {
        return redisTemplate.delete(key)
                .map(count -> count > 0)
                .doOnSuccess(deleted -> log.debug("Deleted cache for key: {}, result: {}", key, deleted))
                .doOnError(error -> log.error("Failed to delete cache for key: {}", key, error));
    }

    public Mono<Boolean> exists(String key) {
        return redisTemplate.hasKey(key)
                .doOnError(error -> log.error("Failed to check existence for key: {}", key, error));
    }

    public Mono<Boolean> expire(String key, Duration ttl) {
        return redisTemplate.expire(key, ttl)
                .doOnSuccess(result -> log.debug("Set expiration for key: {} to {}", key, ttl))
                .doOnError(error -> log.error("Failed to set expiration for key: {}", key, error));
    }

    // Rate limiting helper
    public Mono<Long> increment(String key) {
        return redisTemplate.opsForValue()
                .increment(key)
                .doOnError(error -> log.error("Failed to increment key: {}", key, error));
    }

    // Token blacklist for logout
    public Mono<Boolean> blacklistToken(String token, Duration ttl) {
        String key = "blacklist:token:" + token;
        return set(key, "blacklisted", ttl);
    }

    public Mono<Boolean> isTokenBlacklisted(String token) {
        String key = "blacklist:token:" + token;
        return exists(key);
    }
}