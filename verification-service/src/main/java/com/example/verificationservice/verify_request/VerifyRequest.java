package com.example.verificationservice.verify_request;



import com.example.commondto.constant.Status;
import com.example.commondto.constant.VerifyType;
import com.example.commondto.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    private String userId;

    @Enumerated(EnumType.STRING)
    private VerifyType type; // VEHICLE, JOURNEY, CREDIT

    private String referenceId;

    private String title;

    private String description;

    private List<String> documentUrl;

    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, APPROVED, REJECTED

    private String note;

}