package com.example.vehicleservice.model.entity;

import com.example.commondto.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "journey")
public class Journey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Số km người dùng nhập vào form
    private Double distanceKm;

    private Double energyUsed;

    private Double averageSpeed;

    // Lượng CO2 giảm phát thải (kg)
    private Double co2Reduced;

    @OneToOne(mappedBy = "journey")
    private Vehicle vehicle;
}
