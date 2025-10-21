package com.example.vehicleservice.model.entity;

import com.example.commondto.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "vehicle_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleType extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // 🔹 Hãng xe (VD: Tesla, VinFast, BYD)
    @Column(nullable = false)
    @NotBlank
    private String manufacturer;

    // 🔹 Mẫu xe (VD: Model 3, VF8, Atto 3)
    @Column(nullable = false)
    @NotBlank
    private String model;

    // 🔹 Lượng CO₂ phát thải/km (đơn vị: kg/km)
    @Column(nullable = false)
    @NotNull
    private Double co2PerKm;
}
