package com.example.walletservice.model.entity;

import com.example.commondto.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "carbon_credit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarbonCredit extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String ownerId;
    private Double totalCredit;
    private Double availableCredit;
    private Double tradedCredit;
}
