package com.example.apigateway;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityRuleMatcher {

    private final SecurityProperties securityProperties;

    public SecurityRule match(String path, HttpMethod method) {
        if (securityProperties.getRules() == null) return null;

        return securityProperties.getRules().stream()
                .filter(rule -> pathMatches(path, rule.getPath()))
                .filter(rule -> methodMatches(method, rule.getMethods()))
                .findFirst()
                .orElse(null);
    }

    private boolean pathMatches(String requestPath, String pattern) {
        // Chuyển pattern Spring Cloud Gateway sang regex
        String regex = pattern
                .replace("**", ".*")
                .replace("*", "[^/]*");
        return requestPath.matches(regex);
    }

    private boolean methodMatches(HttpMethod requestMethod, List<String> allowedMethods) {
        if (allowedMethods == null || allowedMethods.isEmpty()) {
            return false;
        }

        // Nếu có "ANY" thì match tất cả
        if (allowedMethods.stream().anyMatch(m -> "ANY".equalsIgnoreCase(m))) {
            return true;
        }

        // Kiểm tra method cụ thể
        return allowedMethods.stream()
                .anyMatch(m -> m.equalsIgnoreCase(requestMethod.name()));
    }
}