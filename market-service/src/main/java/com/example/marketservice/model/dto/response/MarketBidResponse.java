package com.example.marketservice.model.dto.response;

import com.example.marketservice.model.entity.Bid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarketBidResponse {
    private String id;
    private String bidderName;
    private Double amount;

    public static MarketBidResponse from(Bid bid) {
        return MarketBidResponse.builder()
                .id(bid.getId())
                .bidderName(bid.getBidderName())
                .amount(bid.getAmount())
                .build();
    }
}
