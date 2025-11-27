package com.example.walletservice.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_payments", indexes = {
        @Index(name = "idx_correlation_id", columnList = "correlationId", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String correlationId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime processedAt;
}
