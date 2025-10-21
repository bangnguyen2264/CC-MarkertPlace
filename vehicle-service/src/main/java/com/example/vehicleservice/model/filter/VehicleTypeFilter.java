package com.example.vehicleservice.model.filter;

import com.example.commondto.dto.filter.BaseFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class VehicleTypeFilter extends BaseFilter {
    @Schema(
            description = "Hãng sản xuất xe (VD: Tesla, VinFast, BYD,...)",
            example = "VinFast"
    )
    private String manufacturer;

    @Schema(
            description = "Mẫu xe (VD: VF e34, Model 3, Atto 3,...)",
            example = "VF e34"
    )
    private String model;}
