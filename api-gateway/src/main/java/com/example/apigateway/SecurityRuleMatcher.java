package com.example.apigateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityRuleMatcher {

    private final SecurityProperties securityProperties;

    public SecurityRule match(String path, HttpMethod method) {
        if (securityProperties.getRules() == null) {
            log.warn("⚠️ No security rules configured");
            return null;
        }

        for (SecurityRule rule : securityProperties.getRules()) {
            boolean pathMatch = pathMatches(path, rule.getPath());
            boolean methodMatch = methodMatches(method, rule.getMethods());

            log.debug("🔍 Checking rule:");
            log.debug("   rule.path={} | request.path={} | match={}", rule.getPath(), path, pathMatch);
            log.debug("   rule.methods={} | request.method={} | match={}",
                    rule.getMethods(), method, methodMatch);

            if (pathMatch && methodMatch) {
                log.info("🎯 Rule matched: {}", rule);
                return rule;
            }
        }

        log.warn("❌ No rule matched for {} {}", method, path);
        return null;
    }


    private boolean pathMatches(String requestPath, String pattern) {
        if (pattern.endsWith("/**")) {
            String basePath = pattern.substring(0, pattern.length() - 3);
            return requestPath.startsWith(basePath);
        }
        return requestPath.equals(pattern);
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