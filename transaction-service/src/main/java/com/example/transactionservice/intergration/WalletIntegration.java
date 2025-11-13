package com.example.transactionservice.intergration;

import com.example.commondto.dto.request.PaymentRequest;
import com.example.commondto.dto.response.PaymentResponse;
import com.example.commondto.exception.CustomException;
import com.example.transactionservice.kafka.producer.WalletProducer;
import com.example.transactionservice.payment.MarketPaymentResponse;
import com.example.transactionservice.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletIntegration {

    private static final long TIMEOUT_SECONDS = 10;

    private final WalletProducer walletProducer;
    private final ConcurrentHashMap<String, CompletableFuture<PaymentResponse>> pendingRequests = new ConcurrentHashMap<>();

    /**
     * Gửi PaymentRequest đến WalletService và chờ phản hồi qua correlationId
     */
    public MarketPaymentResponse pay(Transaction tx) {

        PaymentRequest request = PaymentRequest.builder()
                .buyerId(tx.getBuyerId())
                .sellerId(tx.getSellerId())
                .amount(tx.getAmount())
                .credit(tx.getCredit())
                .build();

        CompletableFuture<PaymentResponse> future = new CompletableFuture<>();
        pendingRequests.put(request.getCorrelationId(), future);

        log.info("📤 Sending PaymentRequest (txId={}, correlationId={})", tx.getId(), request.getCorrelationId());
        walletProducer.sendPaymentEvent(request);

        // Chờ phản hồi từ WalletService (nếu hết thời gian thì xem như lỗi)
        PaymentResponse response;
        try {
            response = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            pendingRequests.remove(request.getCorrelationId());
            throw new CustomException(
                    "Timeout waiting for WalletService response (txId=" + tx.getId() + ")",
                    HttpStatus.GATEWAY_TIMEOUT
            );
        }

        pendingRequests.remove(request.getCorrelationId());
        log.info("📩 Received PaymentResponse correlationId={} success={} status={}",
                request.getCorrelationId(), response.isSuccess(), response.getStatus());

        // Nếu Wallet báo lỗi → ném CustomException để Global Handler xử lý
        if (!response.isSuccess()) {

            throw new CustomException(
                    "Wallet payment failed: " + response.getMessage() +
                            " (transactionId=" + tx.getId() + ")",
                    response.getStatus()
            );
        }

        // Nếu thành công → trả về MarketPaymentResponse
        return MarketPaymentResponse.builder()
                .transactionId(tx.getId())
                .status("SUCCESS")
                .build();
    }

    /**
     * Hàm này được WalletConsumer gọi khi nhận phản hồi từ WalletService
     */
    public void completeResponse(PaymentResponse response) {
        CompletableFuture<PaymentResponse> future = pendingRequests.remove(response.getCorrelationId());
        if (future != null) {
            future.complete(response);
        } else {
            log.warn("⚠️ No pending request found for correlationId={}", response.getCorrelationId());
        }
    }
}
