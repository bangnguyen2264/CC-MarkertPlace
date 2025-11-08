package com.example.marketservice.model.entity;

import com.example.commondto.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bid")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private MarketListing listing;

    @Column(nullable = false)
    private String bidderId;

    @Column(nullable = false)
    private String bidderName;

    @Column(nullable = false)
    private Double amount;
}
