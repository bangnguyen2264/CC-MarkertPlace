package com.example.vehicleservice.model.dto.response;

import com.example.commondto.constant.Status;
import com.example.vehicleservice.model.entity.JourneyHistory;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JourneyHistoryResponse {
    private String id;
    private Double newDistance;
    private Double averageSpeed;
    private Double energyUsed;
    private List<String> certificateImageUrl;
    private Status status;

    public static JourneyHistoryResponse from (JourneyHistory journeyHistory) {
        return JourneyHistoryResponse.builder()
                .id(journeyHistory.getId())
                .newDistance(journeyHistory.getNewDistance())
                .averageSpeed(journeyHistory.getAverageSpeed())
                .energyUsed(journeyHistory.getEnergyUsed())
                .certificateImageUrl(journeyHistory.getCertificateImageUrl())
                .status(journeyHistory.getStatus())
                .build();
    }

}
