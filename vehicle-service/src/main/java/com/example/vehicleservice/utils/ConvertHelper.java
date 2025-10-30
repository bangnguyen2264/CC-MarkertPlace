package com.example.vehicleservice.utils;

import com.example.commondto.constant.VerifyType;
import com.example.commondto.dto.request.VerifyCreationRequest;
import com.example.vehicleservice.model.entity.JourneyHistory;
import com.example.vehicleservice.model.entity.Vehicle;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ConvertHelper {
    /**
     * Convert Vehicle to VerifyCreationRequest
     */
    public VerifyCreationRequest convertToVerifyCreationRequest(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }

        return VerifyCreationRequest.builder()
                .userId(vehicle.getOwnerId())
                .type(VerifyType.VEHICLE)
                .referenceId(vehicle.getId())
                .title("Vehicle Verification for VIN: " + vehicle.getVin())
                .description("Verification request for vehicle with license plate: " +
                        vehicle.getLicensePlate() + ", registration number: " +
                        vehicle.getRegistrationNumber())
                .documentUrl(vehicle.getRegistrationImageUrl() != null
                        ? vehicle.getRegistrationImageUrl()
                        : Collections.emptyList())
                .build();
    }

    /**
     * Convert JourneyHistory to VerifyCreationRequest
     */
    public VerifyCreationRequest convertToVerifyCreationRequest(JourneyHistory journeyHistory) {
        if (journeyHistory == null) {
            throw new IllegalArgumentException("JourneyHistory cannot be null");
        }


        return VerifyCreationRequest.builder()
                .userId(journeyHistory.getJourney().getVehicle().getOwnerId()) // Lấy updatedBy làm userId
                .type(VerifyType.JOURNEY) // Loại verify là JOURNEY
                .referenceId(journeyHistory.getId()) // ID của JourneyHistory làm referenceId
                .title("Journey Verification for Vehicle: " +
                        (journeyHistory.getJourney().getVehicle() != null ? journeyHistory.getJourney().getVehicle().getVin() : "N/A")) // Title dựa trên journey
                .description("Verification request for journey history with status: " +
                        journeyHistory.getStatus() + ", distance: " +
                        journeyHistory.getNewDistance()) // Tạo description
                .documentUrl(journeyHistory.getCertificateImageUrl() != null
                        ? journeyHistory.getCertificateImageUrl()
                        : Collections.emptyList()) // Sử dụng List<String> trực tiếp
                .build();
    }
}
