package com.example.transactionservice.transaction;

import com.example.commondto.constant.PaymentMethod;
import com.example.commondto.constant.TransactionStatus;
import com.example.commondto.dto.filter.BaseFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TransactionFilter extends BaseFilter {
    private String listingId; // từ MarketListing
    private String buyerId;
    private String sellerId;
    private Double amount;
    private Double credit;
    private TransactionStatus status;
    private PaymentMethod paymentMethod;
    private LocalDateTime paidAt;
}
