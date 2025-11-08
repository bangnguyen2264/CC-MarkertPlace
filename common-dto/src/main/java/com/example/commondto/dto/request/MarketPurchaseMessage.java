package com.example.commondto.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin yêu cầu mua hàng gửi sang TransactionService")
public class MarketPurchaseMessage {

    @Schema(description = "ID listing liên quan đến giao dịch")
    private String referenceId;

    @Schema(description = "Người mua")
    private String buyerId;

    @Schema(description = "Người bán")
    private String sellerId;

    @Schema(description = "Tổng số tiền cần thanh toán")
    private Double amount;

    @Schema(description = "Loại listing (FIXED_PRICE / AUCTION)")
    private String listingType;

}
