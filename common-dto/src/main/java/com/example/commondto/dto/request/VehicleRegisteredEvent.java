package com.example.commondto.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleRegisteredEvent {
    private Long vehicleId;
    private Long ownerId;
    private String licensePlate;
    private String vin;
}
