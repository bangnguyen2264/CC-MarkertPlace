package com.example.marketservice.integration;

import com.example.commondto.dto.response.CarbonCreditValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CarbonCreditValidationIntegration {

    // Lưu map correlationId -> CompletableFuture
    private final Map<String, CompletableFuture<CarbonCreditValidationResponse>> pendingRequests = new ConcurrentHashMap<>();

    /**
     * Đăng ký 1 request cần chờ phản hồi
     */
    public CompletableFuture<CarbonCreditValidationResponse> registerRequest(String correlationId) {
        CompletableFuture<CarbonCreditValidationResponse> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);
        log.debug("🕓 Registered validation request with correlationId={}", correlationId);
        return future;
    }

    /**
     * Khi nhận phản hồi, complete future tương ứng
     */
    public void completeResponse(CarbonCreditValidationResponse response) {
        CompletableFuture<CarbonCreditValidationResponse> future = pendingRequests.remove(response.getCorrelationId());
        if (future != null) {
            future.complete(response);
            log.info("✅ Completed validation for correlationId={}, success={}",
                    response.getCorrelationId(), response.isSuccess());
        } else {
            log.warn("⚠️ No pending request found for correlationId={}", response.getCorrelationId());
        }
    }

    /**
     * Dọn dẹp thủ công (nếu cần)
     */
    public void remove(String correlationId) {
        pendingRequests.remove(correlationId);
    }
}
