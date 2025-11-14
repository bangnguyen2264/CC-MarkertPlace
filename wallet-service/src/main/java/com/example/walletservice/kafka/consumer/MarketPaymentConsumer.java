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
        log.info("Received market payment request: buyer={}, seller={}, amount={}, method={}",
                request.getBuyerId(), request.getSellerId(), request.getAmount(), request.getMethod());

        try {
            Wallet sellerWallet = walletRepository.findByOwnerId(request.getSellerId()).orElse(null);
            if (sellerWallet == null) {
                sendErrorResponse(request, "Seller wallet not found", HttpStatus.NOT_FOUND);
                return;
            }

            // Nếu là thanh toán bằng ví, cần kiểm tra và cập nhật cả buyer
            if (request.getMethod() == PaymentMethod.WALLET) {
                Wallet buyerWallet = walletRepository.findByOwnerId(request.getBuyerId()).orElse(null);
                if (buyerWallet == null) {
                    sendErrorResponse(request, "Buyer wallet not found", HttpStatus.NOT_FOUND);
                    return;
                }

                if (buyerWallet.getBalance() < request.getAmount()) {
                    sendErrorResponse(request, "Insufficient balance in buyer wallet", HttpStatus.BAD_REQUEST);
                    return;
                }

                // Trừ tiền buyer, cộng tiền seller
                buyerWallet.setBalance(buyerWallet.getBalance() - request.getAmount());
                sellerWallet.setBalance(sellerWallet.getBalance() + request.getAmount());
                walletRepository.save(buyerWallet);
                walletRepository.save(sellerWallet);

                // Lưu audit cho buyer (wallet)
                auditService.record(
                        buyerWallet.getOwnerId(),
                        TransactionType.WALLET,
                        TransactionAction.WITHDRAW,
                        request.getAmount(),
                        buyerWallet.getBalance(),
                        auditDescriptionUtil.buildWalletDescription(
                                TransactionAction.WITHDRAW,
                                request.getAmount(),
                                buyerWallet.getOwnerId(),
                                sellerWallet.getOwnerId(),
                                request.getCorrelationId()
                        ),
                        request.getCorrelationId()
                );

                // Lưu audit cho seller (wallet)
                auditService.record(
                        sellerWallet.getOwnerId(),
                        TransactionType.WALLET,
                        TransactionAction.DEPOSIT,
                        request.getAmount(),
                        sellerWallet.getBalance(),
                        auditDescriptionUtil.buildWalletDescription(
                                TransactionAction.DEPOSIT,
                                request.getAmount(),
                                buyerWallet.getOwnerId(),
                                sellerWallet.getOwnerId(),
                                request.getCorrelationId()
                        ),
                        request.getCorrelationId()
                );
            }

            // Lưu audit tín chỉ carbon cho cả buyer và seller
            CarbonCredit buyerCredit = carbonCreditRepository.findByOwnerId(request.getBuyerId()).orElse(null);
            CarbonCredit sellerCredit = carbonCreditRepository.findByOwnerId(request.getSellerId()).orElse(null);

            if (buyerCredit != null && sellerCredit != null) {
                // Buyer + tín chỉ
                carbonCreditService.update(buyerCredit.getId(),
                        CarbonCreditUpdateRequest.builder()
                                .totalCredit(buyerCredit.getTotalCredit() + request.getCredit())
                                .build());

                // Seller - tín chỉ
                carbonCreditService.update(sellerCredit.getId(),
                        CarbonCreditUpdateRequest.builder()
                                .totalCredit(sellerCredit.getTotalCredit() - request.getCredit())
                                .build());

                // Audit buyer carbon
                auditService.record(
                        buyerCredit.getOwnerId(),
                        TransactionType.CARBON_CREDIT,
                        TransactionAction.CREDIT_BUY,
                        request.getCredit(),
                        buyerCredit.getTotalCredit() + request.getCredit(),
                        auditDescriptionUtil.buildCarbonDescription(
                                TransactionAction.CREDIT_BUY,
                                request.getCredit(),
                                sellerCredit.getOwnerId(),
                                buyerCredit.getOwnerId(),
                                request.getCorrelationId()
                        ),
                        request.getCorrelationId()
                );

                // Audit seller carbon
                auditService.record(
                        sellerCredit.getOwnerId(),
                        TransactionType.CARBON_CREDIT,
                        TransactionAction.CREDIT_TRADE,
                        request.getCredit(),
                        sellerCredit.getTotalCredit() - request.getCredit(),
                        auditDescriptionUtil.buildCarbonDescription(
                                TransactionAction.CREDIT_TRADE,
                                request.getCredit(),
                                sellerCredit.getOwnerId(),
                                buyerCredit.getOwnerId(),
                                request.getCorrelationId()
                        ),
                        request.getCorrelationId()
                );
            }

            // Gửi phản hồi thành công
            PaymentResponse response = PaymentResponse.builder()
                    .success(true)
                    .status(HttpStatus.OK)
                    .message("Payment successful")
                    .correlationId(request.getCorrelationId())
                    .build();

            walletProducer.sendMarketPaymentResponse(response);
            log.info("✅ Payment successful via {}: buyer={}, seller={}, amount={}",
                    request.getMethod(), request.getBuyerId(), request.getSellerId(), request.getAmount());

        } catch (Exception e) {
            log.error("Error processing market payment: {}", e.getMessage(), e);
            sendErrorResponse(request, "Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
