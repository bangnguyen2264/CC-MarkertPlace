package com.example.transactionservice.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AppConfig {

    @Autowired
    private Environment env;

    @PostConstruct
    public void testEnv() {
        System.out.println("=== ENV CHECK ===");
        System.out.println("VNP_TMN_CODE = " + env.getProperty("VNP_TMN_CODE"));
        System.out.println("VNP_HASH_SECRET = " + env.getProperty("VNP_HASH_SECRET"));
        System.out.println("VNP_PAY_URL = " + env.getProperty("VNP_PAY_URL"));
        System.out.println("VNP_RETURN_URL = " + env.getProperty("VNP_RETURN_URL"));
    }
}

