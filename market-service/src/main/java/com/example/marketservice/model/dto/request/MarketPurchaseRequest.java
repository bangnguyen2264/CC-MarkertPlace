package com.example.marketservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yêu cầu mua ngay (Buy Now) cho listing dạng giá niêm yết")
public class MarketPurchaseRequest {

    @Schema(description = "ID người mua (buyer)", example = "user-abc123")
    private String buyerId;

    @Schema(description = "ID listing cần mua", example = "listing-xyz789")
    private String listingId;
}
