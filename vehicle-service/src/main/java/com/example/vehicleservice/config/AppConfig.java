package com.example.vehicleservice.config;

import com.example.vehicleservice.model.entity.VehicleType;
import com.example.vehicleservice.repository.VehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AppConfig {

    private final VehicleTypeRepository vehicleTypeRepository;

    @Bean
    public CommandLineRunner init() {
        return args -> {
            log.info("Starting CommandLineRunner to initialize vehicle types");
            try {
                if (!vehicleTypeRepository.existsByManufacturerAndModel("VinFast", "VF e34")) {
                    VehicleType vehicleType = VehicleType.builder()
                            .manufacturer("VinFast")
                            .model("VF e34")
                            .co2PerKm(0.12)
                            .build();
                    log.info("Adding vehicle type: {}", vehicleType);
                    vehicleTypeRepository.save(vehicleType);
                    log.info("Vehicle type saved successfully: {}", vehicleType);
                } else {
                    log.info("Vehicle type VinFast VF e34 already exists, skipping initialization");
                }
            } catch (Exception e) {
                log.error("Error initializing vehicle types: {}", e.getMessage(), e);
                throw e; // Rethrow to ensure the error is visible
            }
        };
    }
}