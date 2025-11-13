package com.example.walletservice.kafka.consumer;

import com.example.commondto.dto.request.PaymentRequest;
import com.example.commondto.dto.response.PaymentResponse;
import com.example.commondto.kafka.KafkaTopics;
import com.example.walletservice.kafka.producer.WalletProducer;
import com.example.walletservice.model.dto.request.CarbonCreditUpdateRequest;
import com.example.walletservice.model.entity.CarbonCredit;
import com.example.walletservice.model.entity.Wallet;
import com.example.walletservice.repository.CarbonCreditRepository;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.service.CarbonCreditService;
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

    @KafkaListener(topics = KafkaTopics.MARKET_PAYMENT_REQUEST,
            groupId = "${spring.application.name}-group",
            containerFactory = "marketPaymentRequestKafkaListenerFactory"
    )
    @Transactional
    public void consumeMarketPaymentRequest(PaymentRequest request) {
        log.info("Received market payment request: buyer={}, seller={}, amount={}", request.getBuyerId(), request.getSellerId(), request.getAmount());

        try {
            Wallet buyerWallet = walletRepository.findByOwnerId(request.getBuyerId()).orElse(null);
            if (buyerWallet == null) {
                sendErrorResponse(request, "Buyer wallet not found for ownerId=" + request.getBuyerId(), HttpStatus.NOT_FOUND);
                return;
            }

            Wallet sellerWallet = walletRepository.findByOwnerId(request.getSellerId()).orElse(null);
            if (sellerWallet == null) {
                sendErrorResponse(request, "Seller wallet not found for ownerId=" + request.getSellerId(), HttpStatus.NOT_FOUND);
                return;
            }

            // Kiểm tra số dư
            if (buyerWallet.getBalance() < request.getAmount()) {
                log.info("Buyer wallet balance {}  is lower than seller's balance {}",buyerWallet.getBalance(), request.getAmount());
                sendErrorResponse(request , "Insufficient balance in buyer wallet", HttpStatus.BAD_REQUEST);
                return;
            }
            CarbonCredit buyerCredit = carbonCreditRepository.findByOwnerId(request.getBuyerId()).orElse(null);
            if (buyerCredit == null) {
                sendErrorResponse(request, "Buyer cc not found for ownerId=" + request.getSellerId(), HttpStatus.NOT_FOUND);
                return;
            }
            // Thực hiện giao dịch
            Double currentBuyerBalance = buyerWallet.getBalance() != null ? buyerWallet.getBalance() : 0.0;
            buyerWallet.setBalance(currentBuyerBalance - request.getAmount());
            Double currentSellerBalance = sellerWallet.getBalance() != null ? sellerWallet.getBalance() : 0.0;
            sellerWallet.setBalance(currentSellerBalance + request.getAmount());

            // Nạp tín chỉ
            carbonCreditService.update(buyerCredit.getId(), CarbonCreditUpdateRequest.builder()
                    .totalCredit(buyerCredit.getTotalCredit() + request.getCredit())
                    .build());
            walletRepository.save(buyerWallet);
            walletRepository.save(sellerWallet);

            // Gửi phản hồi thành công
            PaymentResponse response = PaymentResponse.builder().status(HttpStatus.OK).message("Payment successful").success(true).correlationId(request.getCorrelationId()).build();
            walletProducer.sendMarketPaymentResponse(response);
            log.info("Payment successful: buyer={}, seller={}, amount={}", request.getBuyerId(), request.getSellerId(), request.getAmount());

        } catch (Exception e) {
            log.error("Error processing market payment: {}", e.getMessage(), e);
            sendErrorResponse(request, "Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendErrorResponse(PaymentRequest request, String message, HttpStatus status) {
        PaymentResponse response = PaymentResponse.builder().status(status).message(message).success(false).correlationId(request.getCorrelationId()).build();

        walletProducer.sendMarketPaymentResponse(response);
        log.warn("Payment failed: {} | buyer={} seller={} status={}", message, request.getBuyerId(), request.getSellerId(), status);
    }
}
