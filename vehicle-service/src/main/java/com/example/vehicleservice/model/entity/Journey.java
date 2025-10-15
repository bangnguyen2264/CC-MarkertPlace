package com.example.vehicleservice.model.entity;

import com.example.vehicleservice.model.constants.JourneyStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "journeys")
public class Journey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết 1–1 tới Vehicle
    @OneToOne
    @JoinColumn(name = "vehicle_id", unique = true)
    private Vehicle vehicle;

    // Số km người dùng nhập vào form
    private double distanceKm;

    // Thời gian bắt đầu / kết thúc hành trình (người dùng nhập hoặc giả lập)
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Lượng CO2 giảm phát thải (kg)
    private double co2Reduced;

    // Trạng thái: PENDING, VERIFIED, REJECTED
    @Enumerated(EnumType.STRING)
    private JourneyStatus status;

    // Ghi chú từ CVA hoặc Admin
    private String verificationNote;

    // Ngày tạo
    private LocalDateTime createdAt;
}
