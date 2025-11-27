package com.example.walletservice.service;

public interface ProcessedPaymentService {
    /**
     * Kiểm tra xem correlationId đã được xử lý chưa
     */
    boolean isProcessed(String correlationId);

    /**
     * Đánh dấu correlationId đã được xử lý
     */
    void markAsProcessed(String correlationId);
}
