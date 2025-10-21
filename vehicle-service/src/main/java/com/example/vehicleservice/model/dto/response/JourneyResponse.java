package com.example.vehicleservice.model.dto.response;

import com.example.vehicleservice.model.entity.Journey;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JourneyResponse {
    private String id;

    // Số km người dùng nhập vào form
    private double distanceKm;
    // Năng lượng đã sử dụng
    private double energyUsed;

    // Lượng CO2 giảm phát thải (kg)
    private double co2Reduced;

    public static JourneyResponse from(Journey journey) {
        return JourneyResponse.builder()
                .id(journey.getId())
                .distanceKm(journey.getDistanceKm())
                .energyUsed(journey.getEnergyUsed())
                .co2Reduced(journey.getCo2Reduced())
                .build();
    }
}
