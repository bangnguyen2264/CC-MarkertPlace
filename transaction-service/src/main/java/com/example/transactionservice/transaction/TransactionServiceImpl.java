package com.example.transactionservice.transaction;

import com.example.commondto.constant.PaymentMethod;
import com.example.commondto.constant.TransactionStatus;
import com.example.commondto.dto.request.MarketPurchaseMessage;
import com.example.commondto.exception.BadRequestException;
import com.example.commondto.exception.NotFoundException;
import com.example.commondto.utils.CrudUtils;
import com.example.transactionservice.intergration.WalletIntegration;
import com.example.transactionservice.kafka.producer.TransactionProducer;
import com.example.transactionservice.payment.MarketPaymentResponse;
import com.example.transactionservice.vnpay.VNPayService;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final WalletIntegration walletIntegration;
    private final VNPayService vnPayService;
    private final TransactionProducer transactionProducer;

    @Override
    public Transaction createPendingTransaction(MarketPurchaseMessage message) {
        Transaction tx = Transaction.builder()
                .listingId(message.getListingId())
                .buyerId(message.getBuyerId())
                .sellerId(message.getSellerId())
                .amount(message.getAmount())
                .credit(message.getCredit())
                .status(TransactionStatus.PENDING_PAYMENT)
                .build();

        repository.save(tx);

        log.info("🧾 Created pending transaction: {}", tx.getId());
        return tx;
    }

    @Override
    public Transaction getById(String id) {
        return repository.findById(id).orElseThrow(
                () -> new NotFoundException("Transaction not found")
        );
    }

    @Override
    public List<Transaction> getAll(TransactionFilter filter) {
        Pageable pageable = CrudUtils.createPageable(filter);
        Specification<Transaction> spec = _buildFilter(filter);

        Page<Transaction> page = repository.findAll(spec, pageable);
        return page.getContent();
    }

    @Override
    public Transaction update(String id, TransactionStatus status) {
        // 1. Kiểm tra transaction tồn tại
        Transaction tx = getById(id);
        // 2. Cập nhật status
        tx.setStatus(status);

        // 3. Chỉ xử lý payment nếu là SUCCESS và bằng VN_PAY
        if (status == TransactionStatus.SUCCESS
                && tx.getPaymentMethod() != null
                && tx.getPaymentMethod() == PaymentMethod.VN_PAY) {

                MarketPaymentResponse response = walletIntegration.pay(tx);

                // Kiểm tra response và status
                if (response != null && "SUCCESS".equalsIgnoreCase(response.getStatus())) {
                    // Gửi Kafka chỉ khi mọi thứ hợp lệ và producer tồn tại
                    if (transactionProducer != null) {
                        transactionProducer.sendPaymentEvent(tx.getListingId());
                    }

                }
        }

        // 4. Lưu thay đổi
        return repository.save(tx);
    }

    private Specification<Transaction> _buildFilter(TransactionFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by listingId
            if (filter.getListingId() != null && !filter.getListingId().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("listingId"), filter.getListingId()));
            }

            // Filter by buyerId
            if (filter.getBuyerId() != null && !filter.getBuyerId().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("buyerId"), filter.getBuyerId()));
            }

            // Filter by sellerId
            if (filter.getSellerId() != null && !filter.getSellerId().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("sellerId"), filter.getSellerId()));
            }

            // Filter by amount
            if (filter.getAmount() != null) {
                predicates.add(criteriaBuilder.equal(root.get("amount"), filter.getAmount()));
            }

            // Filter by credit
            if (filter.getCredit() != null) {
                predicates.add(criteriaBuilder.equal(root.get("credit"), filter.getCredit()));
            }

            // Filter by status
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Filter by paymentMethod
            if (filter.getPaymentMethod() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), filter.getPaymentMethod()));
            }


            // Filter by paidAt
            if (filter.getPaidAt() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paidAt"), filter.getPaidAt()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    @Transactional
    public MarketPaymentResponse initiatePayment(String transactionId, PaymentMethod method, String clientIp) {
        Transaction tx = repository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (tx.getStatus() != TransactionStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Transaction not pending payment");
        }

        tx.setPaymentMethod(method);

        MarketPaymentResponse response;
        switch (method) {
            case WALLET -> {
                response = walletIntegration.pay(tx);
                tx.setStatus(TransactionStatus.SUCCESS);
                tx.setPaidAt(LocalDateTime.now());
                transactionProducer.sendPaymentEvent(tx.getListingId());
                log.info("💰 Wallet payment success for transaction {}", tx.getId());
            }
            case VN_PAY -> {
                response = vnPayService.pay(tx, clientIp);
                tx.setPaymentUrl(response.getPaymentUrl());
                tx.setStatus(TransactionStatus.PENDING_PAYMENT);
                log.info("🔗 VNPay payment URL generated for transaction {}", tx.getId());
            }
            default -> throw new BadRequestException("Unsupported payment method: " + method);
        }

        repository.save(tx);
        return response;
    }

    @Override
    @Transactional
    public void handlePaymentCallback(String transactionId, boolean success) {
        Transaction tx = repository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (success) {
            tx.setStatus(TransactionStatus.SUCCESS);
            tx.setPaidAt(LocalDateTime.now());
            log.info("VNPay callback success for transaction {}", tx.getId());
            // producer.sendTransactionCompleted(tx);
        } else {
            tx.setStatus(TransactionStatus.FAILED);
            log.warn(" VNPay callback failed for transaction {}", tx.getId());
            // producer.sendTransactionFailed(tx);
        }

        repository.save(tx);
    }

    @Override
    @Transactional
    public MarketPaymentResponse pay(String listingId, PaymentMethod paymentMethod, String clientIp) {
        Transaction tx = repository.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        return initiatePayment(tx.getId(), paymentMethod, clientIp);
    }
}
