package com.example.vehicleservice.model.dto.request;

import com.example.vehicleservice.model.constants.JourneyStatus;
import com.example.vehicleservice.model.entity.JourneyHistory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JourneyHistoryRequest {
    private String journeyId;
    private Double newDistance;
    private Double averageSpeed;
    private Double energyUsed;
    private String certificateImageUrl;

    public static JourneyHistory to(JourneyHistoryRequest journeyHistoryRequest) {
        return JourneyHistory.builder()
                .newDistance(journeyHistoryRequest.getNewDistance())
                .averageSpeed(journeyHistoryRequest.getAverageSpeed())
                .energyUsed(journeyHistoryRequest.getEnergyUsed())
                .certificateImageUrl(journeyHistoryRequest.getCertificateImageUrl())
                .status(JourneyStatus.PENDING)
                .build();
    }
}
