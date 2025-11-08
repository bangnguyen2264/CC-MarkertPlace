package com.example.marketservice.model.entity;

import com.example.commondto.constant.ListingStatus;
import com.example.commondto.constant.ListingType;
import com.example.commondto.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "market_listing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketListing extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String sellerId;

    @Column(nullable = false)
    private String creditId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingType type;

    @Column(nullable = false)
    private Double pricePerCredit;

    private Double startingPrice;
    private Double highestBid;
    private String highestBidderId;

    @Column(nullable = false)
    private Double quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Bid> bids = new ArrayList<>();
}
