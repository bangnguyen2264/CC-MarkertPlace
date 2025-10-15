package com.example.vehicleservice.model.dto.response;

import com.example.vehicleservice.model.entity.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * DTO trả về thông tin loại xe điện
 */
@Data
@Builder
public class VehicleTypeResponse {

    @Schema(description = "ID của loại xe", example = "1")
    private Long id;

    @Schema(description = "Hãng sản xuất xe", example = "VinFast")
    private String manufacturer;

    @Schema(description = "Mẫu xe cụ thể", example = "VF e34")
    private String model;

    @Schema(description = "Lượng phát thải CO₂ trên mỗi km (kg/km)", example = "0.12")
    private double co2PerKm;

    public static VehicleTypeResponse from(VehicleType vehicleType) {
        return VehicleTypeResponse.builder()
                .id(vehicleType.getId())
                .manufacturer(vehicleType.getManufacturer())
                .model(vehicleType.getModel())
                .co2PerKm(vehicleType.getCo2PerKm())
                .build();
    }
}
