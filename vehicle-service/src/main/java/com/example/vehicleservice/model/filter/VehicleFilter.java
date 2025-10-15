package com.example.vehicleservice.model.filter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class VehicleFilter extends BaseFilter {

    private Long ownerId;
    private String vin;
    private Long vehicleTypeId;
    private boolean enabled;
}
