package com.example.marketservice.model.dto.response;

import com.example.commondto.constant.ListingStatus;
import com.example.commondto.constant.ListingType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MarketListingResponse {
    private String id;
    private String sellerId;
    private double pricePerCredit;
    private List<MarketBidResponse> bidResponseList;
    private Double quantity;
    private ListingType type;
    private ListingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime endTime;

}
