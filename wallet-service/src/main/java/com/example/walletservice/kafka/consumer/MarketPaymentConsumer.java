package com.example.walletservice.kafka.consumer;

import com.example.commondto.constant.PaymentMethod;
import com.example.commondto.constant.TransactionAction;
import com.example.commondto.constant.TransactionType;
import com.example.commondto.dto.request.PaymentRequest;
import com.example.commondto.dto.response.PaymentResponse;
import com.example.commondto.kafka.KafkaTopics;
import com.example.walletservice.kafka.producer.WalletProducer;
import com.example.walletservice.model.dto.request.CarbonCreditUpdateRequest;
import com.example.walletservice.model.entity.CarbonCredit;
import com.example.walletservice.model.entity.Wallet;
import com.example.walletservice.repository.CarbonCreditRepository;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.service.AuditService;
import com.example.walletservice.service.CarbonCreditService;
import com.example.walletservice.utils.AuditDescriptionUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketPaymentConsumer {

    private final WalletRepository walletRepository;
    private final CarbonCreditRepository carbonCreditRepository;
    private final CarbonCreditService carbonCreditService;
    private final WalletProducer walletProducer;
    private final AuditService auditService;
    private final AuditDescriptionUtil auditDescriptionUtil;

    @KafkaListener(
            topics = KafkaTopics.MARKET_PAYMENT_REQUEST,
            groupId = "${spring.application.name}-group",
            containerFactory = "marketPaymentRequestKafkaListenerFactory"
    )
    @Transactional
    public void consumeMarketPaymentRequest(PaymentRequest request) {
        log.info("Processing market payment: buyer={}, seller={}, amount={}, credit={}, method={}, corr={}",
                request.getBuyerId(), request.getSellerId(), request.getAmount(), request.getCredit(),
                request.getMethod(), request.getCorrelationId());

        try {
            // === 1. Lấy Wallet & CarbonCredit (bắt buộc phải tồn tại) ===
            Wallet sellerWallet = walletRepository.findByOwnerId(request.getSellerId())
                    .orElse(null);
            Wallet buyerWallet = request.getMethod() == PaymentMethod.WALLET
                    ? walletRepository.findByOwnerId(request.getBuyerId()).orElse(null)
                    : null;

            CarbonCredit buyerCredit = carbonCreditRepository.findByOwnerId(request.getBuyerId())
                    .orElse(null);
            CarbonCredit sellerCredit = carbonCreditRepository.findByOwnerId(request.getSellerId())
                    .orElse(null);

            // Kiểm tra tồn tại
            if (sellerWallet == null || buyerCredit == null || sellerCredit == null
                    || (request.getMethod() == PaymentMethod.WALLET && buyerWallet == null)) {

                String missing = "";
                if (sellerWallet == null) missing += "seller_wallet ";
                if (buyerCredit == null) missing += "buyer_carbon ";
                if (sellerCredit == null) missing += "seller_carbon ";
                if (request.getMethod() == PaymentMethod.WALLET && buyerWallet == null) missing += "buyer_wallet";

                sendErrorResponse(request, "Missing required entities: " + missing.trim(), HttpStatus.NOT_FOUND);
                return;
            }

            // === 2. Xử lý tiền ví (chỉ khi dùng WALLET) ===
            if (request.getMethod() == PaymentMethod.WALLET) {
                if (buyerWallet.getBalance() < request.getAmount()) {
                    sendErrorResponse(request, "Insufficient balance in buyer wallet", HttpStatus.PAYMENT_REQUIRED);
                    return;
                }

                // Trừ tiền người mua
                buyerWallet.setBalance(buyerWallet.getBalance() - request.getAmount());
                walletRepository.save(buyerWallet);

                // Audit: người mua bị trừ tiền
                auditService.record(
                        request.getBuyerId(),
                        TransactionType.WALLET,
                        TransactionAction.WITHDRAW,
                        request.getAmount(),
                        buyerWallet.getBalance(),
                        auditDescriptionUtil.buildWalletDescription(
                                TransactionAction.WITHDRAW,
                                request.getAmount(),
                                request.getBuyerId(),
                                request.getSellerId(),
                                request.getCorrelationId()
                        ),
                        request.getCorrelationId()
                );
            }

            // === 3. Cộng tiền cho người bán (LUÔN LUÔN) ===
            sellerWallet.setBalance(sellerWallet.getBalance() + request.getAmount());
            walletRepository.save(sellerWallet);

            // Audit: người bán nhận tiền
            auditService.record(
                    request.getSellerId(),
                    TransactionType.WALLET,
                    TransactionAction.DEPOSIT,
                    request.getAmount(),
                    sellerWallet.getBalance(),
                    auditDescriptionUtil.buildWalletDescription(
                            TransactionAction.DEPOSIT,
                            request.getAmount(),
                            request.getBuyerId(),
                            request.getSellerId(),
                            request.getCorrelationId()
                    ),
                    request.getCorrelationId()
            );

            // === 4. Cộng tín chỉ cho người mua (LUÔN LUÔN) ===
            carbonCreditService.update(buyerCredit.getId(),
                    CarbonCreditUpdateRequest.builder()
                            .totalCredit(buyerCredit.getTotalCredit() + request.getCredit())
                            .build());

            // Audit: người mua nhận tín chỉ
            auditService.record(
                    request.getBuyerId(),
                    TransactionType.CARBON_CREDIT,
                    TransactionAction.CREDIT_BUY,
                    request.getCredit(),
                    buyerCredit.getTotalCredit() + request.getCredit(),
                    auditDescriptionUtil.buildCarbonDescription(
                            TransactionAction.CREDIT_BUY,
                            request.getCredit(),
                            request.getSellerId(),
                            request.getBuyerId(),
                            request.getCorrelationId()
                    ),
                    request.getCorrelationId()
            );

            // Audit: người bán đã bán tín chỉ (đã bị khóa từ trước)
            auditService.record(
                    request.getSellerId(),
                    TransactionType.CARBON_CREDIT,
                    TransactionAction.CREDIT_TRADE,
                    request.getCredit(),
                    sellerCredit.getTotalCredit(), // số dư hiện tại (đã trừ trước đó)
                    auditDescriptionUtil.buildCarbonDescription(
                            TransactionAction.CREDIT_TRADE,
                            request.getCredit(),
                            request.getSellerId(),
                            request.getBuyerId(),
                            request.getCorrelationId()
                    ),
                    request.getCorrelationId()
            );

            // === 5. Gửi phản hồi thành công ===
            PaymentResponse response = PaymentResponse.builder()
                    .success(true)
                    .status(HttpStatus.OK)
                    .message("Payment and credit transfer completed successfully")
                    .correlationId(request.getCorrelationId())
                    .build();

            walletProducer.sendMarketPaymentResponse(response);

            log.info("Market payment SUCCESS | Method: {} | Buyer {} +{} credits | Seller {} +{} VND | Corr: {}",
                    request.getMethod(), request.getBuyerId(), request.getCredit(),
                    request.getSellerId(), request.getAmount(), request.getCorrelationId());

        } catch (Exception e) {
            log.error("Failed to process market payment request: {}", e.getMessage(), e);
            sendErrorResponse(request, "Internal processing error", HttpStatus.INTERNAL_SERVER_ERROR);
            // @Transactional sẽ tự rollback
        }
    }

    private void sendErrorResponse(PaymentRequest request, String message, HttpStatus status) {
        PaymentResponse response = PaymentResponse.builder()
                .success(false)
                .status(status)
                .message(message)
                .correlationId(request.getCorrelationId())
                .build();

        walletProducer.sendMarketPaymentResponse(response);
        log.warn("❌ Payment failed: {} | buyer={} seller={} status={}", message, request.getBuyerId(), request.getSellerId(), status);
    }
}
