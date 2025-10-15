package com.example.vehicleservice.model.dto.request;

import com.example.vehicleservice.model.entity.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class VehicleTypeRequest {

    @Schema(
            description = "Hãng sản xuất xe (VD: Tesla, VinFast, BYD,...)",
            example = "VinFast",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;

    @Schema(
            description = "Mẫu xe (VD: VF e34, Model 3, Atto 3,...)",
            example = "VF e34",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Model is required")
    private String model;

    @Schema(
            description = "Lượng CO₂ phát thải trên mỗi km (đơn vị: kg/km)",
            example = "0.12",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "CO2 emission per km is required")
    private Double co2PerKm;

    public static VehicleType to(VehicleTypeRequest vehicleTypeRequest) {
        return VehicleType.builder()
                .manufacturer(vehicleTypeRequest.getManufacturer())
                .model(vehicleTypeRequest.getModel())
                .co2PerKm(vehicleTypeRequest.getCo2PerKm())
                .build();
    }
}
