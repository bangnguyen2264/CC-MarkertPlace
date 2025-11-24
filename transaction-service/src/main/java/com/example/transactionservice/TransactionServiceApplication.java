package com.example.transactionservice;

import com.example.transactionservice.vnpay.VNPayConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.example.transactionservice",
		"com.example.commondto"
})
public class TransactionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionServiceApplication.class, args);
	}
	@PostConstruct
	public void logConfig() {
		VNPayConfig config = new VNPayConfig();
		System.out.println("Fix kafka 1");
		System.out.println("✅ VNPay Config loaded:");
		System.out.println(" - API URL: " + config.getApiUrl());
		System.out.println(" - Return URL: " + config.getReturnUrl());
	}

}
