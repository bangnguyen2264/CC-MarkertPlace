package com.example.vehicleservice.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleRequest {
    private Long ownerId;
    private String vin;
    private String licensePlate;
    private String registrationNumber;
    private String color;
    private LocalDate registrationDate;
    private Long mileage;
    private Long vehicleTypeId;
    private String registrationImageUrl;
    private String note;
}

