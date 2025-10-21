package com.example.vehicleservice.model.dto.request;

import com.example.vehicleservice.model.constants.JourneyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateJourneyHistoryRequest {
    @NotNull
    private JourneyStatus status;
    private String note;
}
