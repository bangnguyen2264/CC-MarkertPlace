package com.example.transactionservice.vnpay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "vnpay")
public class VNPayConfig {
    private String tmnCode;
    private String secretKey;
    private String apiUrl;
    private String returnUrl;
    private String version;
    private String currCode;
}