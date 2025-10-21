package com.example.vehicleservice.model.dto.response;
import com.example.vehicleservice.model.entity.Vehicle;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {

    private String id;

    private String ownerId;

    private String vin;

    private String licensePlate;

    private String registrationNumber;

    private String color;

    private LocalDate registrationDate;

    private Long mileage;

    private boolean verified;

    private String registrationImageUrl;

    private String note;
    private JourneyResponse journey;

    private VehicleTypeResponse vehicleType;

    public static VehicleResponse from(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .ownerId(vehicle.getOwnerId())
                .vehicleType(VehicleTypeResponse.from(vehicle.getVehicleType()))
                .journey(JourneyResponse.from(vehicle.getJourney()))
                .vin(vehicle.getVin())
                .licensePlate(vehicle.getLicensePlate())
                .registrationNumber(vehicle.getRegistrationNumber())
                .color(vehicle.getColor())
                .registrationDate(vehicle.getRegistrationDate())
                .mileage(vehicle.getMileage())
                .verified(vehicle.isVerified())
                .registrationImageUrl(vehicle.getRegistrationImageUrl())
                .note(vehicle.getNote())
                .build();
    }
}
