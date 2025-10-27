package com.example.verificationservice.verify_request;


import com.example.commondto.constant.VerifyStatus;
import com.example.commondto.constant.VerifyType;
import com.example.commondto.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "verify_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private VerifyType type; // VEHICLE, JOURNEY, CREDIT

    private Long referenceId;

    private String title;

    private String description;

    private String documentUrl;

    @Enumerated(EnumType.STRING)
    private VerifyStatus status; // PENDING, APPROVED, REJECTED

}