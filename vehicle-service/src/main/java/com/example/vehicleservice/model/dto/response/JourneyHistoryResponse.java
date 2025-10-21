package com.example.vehicleservice.model.dto.response;

import com.example.vehicleservice.model.constants.JourneyStatus;
import com.example.vehicleservice.model.entity.JourneyHistory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JourneyHistoryResponse {
    private String id;
    private Double newDistance;
    private Double averageSpeed;
    private Double energyUsed;
    private String certificateImageUrl;
    private String updatedBy; // CVA hoặc Admin
    private JourneyStatus status;

    public static JourneyHistoryResponse from (JourneyHistory journeyHistory) {
        return JourneyHistoryResponse.builder()
                .id(journeyHistory.getId())
                .newDistance(journeyHistory.getNewDistance())
                .averageSpeed(journeyHistory.getAverageSpeed())
                .energyUsed(journeyHistory.getEnergyUsed())
                .certificateImageUrl(journeyHistory.getCertificateImageUrl())
                .updatedBy(journeyHistory.getUpdatedBy())
                .status(journeyHistory.getStatus())
                .build();
    }

}
