package com.example.apigateway;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.cloud.gateway.security")
public class SecurityProperties {
    private List<SecurityRule> rules;
}
