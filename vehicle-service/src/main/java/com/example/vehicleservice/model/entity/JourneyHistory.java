package com.example.vehicleservice.model.entity;

import com.example.commondto.constant.Status;
import com.example.commondto.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "journey_histories")
public class JourneyHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private Double newDistance;
    private Double averageSpeed;
    private Double energyUsed;
    private List<String> certificateImageUrl;
    private String note;
    private String updatedBy; // CVA hoặc Admin
    private Status status;
    @ManyToOne
    @JoinColumn(name = "journey_id")
    private Journey journey;
}
