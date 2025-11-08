package com.example.marketservice.model.dto.request;

import com.example.commondto.constant.ListingType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
public class MarketListingRequest {
    @NotBlank
    private String sellerId;
    @NotBlank
    private String creditId;
    @NotNull
    @Positive
    private Double pricePerCredit;
    @NotNull
    @Positive
    private Double quantity;
    @NotNull
    private ListingType type; // FIXED_PRICE hoặc AUCTION
    @NotNull
    private OffsetDateTime endTime;
}