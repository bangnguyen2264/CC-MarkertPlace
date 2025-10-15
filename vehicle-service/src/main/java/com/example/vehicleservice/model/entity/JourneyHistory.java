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
@Table(name = "journey_histories")
public class JourneyHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "journey_id")
    private Journey journey;

    @Enumerated(EnumType.STRING)
    private JourneyStatus previousStatus;

    @Enumerated(EnumType.STRING)
    private JourneyStatus newStatus;

    private String note;
    private String updatedBy; // CVA hoặc Admin
}
