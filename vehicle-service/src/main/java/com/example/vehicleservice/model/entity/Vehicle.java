package com.example.vehicleservice.model.entity;

import com.example.commondto.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String ownerId;

    @Column(nullable = false, unique = true)
    private String vin;

    @Column(nullable = false, unique = true)
    private String licensePlate;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    private String color;

    private LocalDate registrationDate;

    private Long mileage;

    private boolean verified = false;

    private String registrationImageUrl;

    private String note;
    // Liên kết 1–1 tới Vehicle
    @OneToOne(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "journey_id", unique = true)
    private Journey journey;

    // 🔹 Tham chiếu loại xe
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_type_id", nullable = false)
    private VehicleType vehicleType;
}

