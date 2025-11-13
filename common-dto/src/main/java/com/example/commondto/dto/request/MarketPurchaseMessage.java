package com.example.commondto.dto.request;

import com.example.commondto.constant.ListingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank
    private String listingId;

    @Schema(description = "Người mua")
    @NotBlank
    private String buyerId;

    @Schema(description = "Người bán")
    @NotBlank
    private String sellerId;

    @Schema(description = "Tổng số tiền cần thanh toán")
    @NotNull
    private Double amount;

    @Schema(description = "Tổng số tín chỉ cần thanh toán")
    @NotNull
    private Double credit;


    @Schema(description = "Loại listing (FIXED_PRICE / AUCTION)")
    private ListingType listingType;

}
