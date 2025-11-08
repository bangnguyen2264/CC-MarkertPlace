package com.example.marketservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu đặt giá cho phiên đấu giá (Auction bid)")
public class MarketBidRequest {

    @Schema(description = "ID người đấu giá (bidder)")
    private String bidderId;

    @Schema(description = "Tên người đấu giá (bidder)", example = "user")
    private String bidderName;

    @Schema(description = "ID listing đang đấu giá", example = "listing-xyz789")
    private String listingId;

    @Schema(description = "Giá đặt của người đấu giá", example = "150.0")
    private Double bidAmount;
}
