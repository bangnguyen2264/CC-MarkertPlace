package com.example.walletservice.utils;

import com.example.commondto.constant.TransactionAction;
import com.example.commondto.constant.TransactionType;
import org.springframework.stereotype.Component;

@Component
public class AuditDescriptionUtil {

    /**
     * Mô tả giao dịch ví tiền (WALLET)
     *
     * @param action        Loại hành động (DEPOSIT / WITHDRAW / TRANSFER)
     * @param amount        Số tiền
     * @param sourceId      ID người gửi / người thực hiện
     * @param targetId      ID người nhận
     * @param correlationId ID tham chiếu giao dịch
     */
    public String buildWalletDescription(TransactionAction action,
                                         Double amount,
                                         String sourceId,
                                         String targetId,
                                         String correlationId) {

        String symbol = (action == TransactionAction.WITHDRAW)
                ? "−"
                : "+";

        String direction;
        switch (action) {
            case DEPOSIT -> direction = String.format("to Wallet (from %s)", sourceId);
            case WITHDRAW -> direction = String.format("from Wallet (to %s)", targetId);
            default -> direction = "wallet operation";
        }

        return String.format("%s%.2f %s | TxID=%s", symbol, amount, direction, correlationId);
    }

    /**
     * Mô tả giao dịch tín chỉ carbon (CARBON_CREDIT)
     *
     * @param action        Loại hành động (CREDIT_BUY / CREDIT_TRADE / CREDIT_TOP_UP)
     * @param credit        Số lượng tín chỉ
     * @param sourceId      ID người bán / nguồn
     * @param targetId      ID người mua / đích
     * @param correlationId ID giao dịch
     */
    public String buildCarbonDescription(TransactionAction action,
                                         Double credit,
                                         String sourceId,
                                         String targetId,
                                         String correlationId) {

        String symbol = (action == TransactionAction.CREDIT_TRADE)
                ? "−"
                : "+";

        String direction;
        switch (action) {
            case CREDIT_BUY ->
                    direction = String.format("carbon credits (bought from %s)", sourceId);
            case CREDIT_TRADE ->
                    direction = String.format("carbon credits (sold to %s)", targetId);
            case CREDIT_TOP_UP ->
                    direction = "carbon credits (top-up)";
            default -> direction = "carbon credit operation";
        }

        return String.format("%s%.2f %s | TxID=%s", symbol, credit, direction, correlationId);
    }

    /**
     * Helper cho log dạng ngắn — ví dụ dùng trong auditService hoặc log hệ thống.
     */
    public String summary(TransactionType type, TransactionAction action, String description) {
        return String.format("[%s - %s] %s", type, action, description);
    }
}
