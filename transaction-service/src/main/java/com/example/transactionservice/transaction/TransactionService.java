package com.example.transactionservice.transaction;

import com.example.commondto.constant.PaymentMethod;
import com.example.commondto.constant.TransactionStatus;
import com.example.commondto.dto.request.MarketPurchaseMessage;
import com.example.transactionservice.payment.MarketPaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface TransactionService {

    Transaction createPendingTransaction(MarketPurchaseMessage message);

    Transaction getById(String id);

    List<Transaction> getAll(TransactionFilter filter);

    Transaction update(String id, TransactionStatus status);

    MarketPaymentResponse initiatePayment(String transactionId, PaymentMethod method, String clientIp);

    void handlePaymentCallback(String transactionId, boolean success);

    MarketPaymentResponse pay(String listingId, PaymentMethod paymentMethod, String clientIp);
}
