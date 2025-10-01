package com.example.userservice.config;

import com.example.userservice.model.entity.Role;
import com.example.userservice.model.entity.User;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAdminAccount() {
        return args -> {
            // Check if admin account already exists
            if (!userRepository.existsByEmail("admin@example.com")) {
                User admin = User.builder()
                        .fullName("Administrator")
                        .email("admin@example.com")
                        .phoneNumber("+1234567890")
                        .dob(LocalDate.of(1990, 1, 1))
                        .role(Role.ADMIN)
                        .password(passwordEncoder.encode("Admin@123"))
                        .build();
                userRepository.save(admin);
                System.out.println("Admin account created: admin@example.com");
            } else {
                System.out.println("Admin account already exists: admin@example.com");
            }
        };
    }
}