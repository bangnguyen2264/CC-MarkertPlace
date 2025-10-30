package com.example.vehicleservice.model.dto.request;

import com.example.commondto.constant.Status;
import com.example.vehicleservice.model.entity.JourneyHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JourneyHistoryRequest {
    private String journeyId;
    private Double newDistance;
    private Double averageSpeed;
    private Double energyUsed;
    private List<String> certificateImageUrl;

    public static JourneyHistory to(JourneyHistoryRequest journeyHistoryRequest) {
        return JourneyHistory.builder()
                .newDistance(journeyHistoryRequest.getNewDistance())
                .averageSpeed(journeyHistoryRequest.getAverageSpeed())
                .energyUsed(journeyHistoryRequest.getEnergyUsed())
                .certificateImageUrl(journeyHistoryRequest.getCertificateImageUrl())
                .status(Status.PENDING)
                .build();
    }
}
