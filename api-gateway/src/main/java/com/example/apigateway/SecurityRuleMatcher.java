package com.example.apigateway;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityRuleMatcher {

    private final SecurityProperties securityProperties;

    public SecurityRule match(String path, HttpMethod method) {
        if (securityProperties.getRules() == null) return null;

        return securityProperties.getRules().stream()
                .filter(rule -> path.matches(rule.getPath().replace("**", ".*")))
                .filter(rule -> rule.getMethod().equalsIgnoreCase("ANY") ||
                        rule.getMethod().equalsIgnoreCase(method.name()))
                .findFirst()
                .orElse(null);
    }
}

