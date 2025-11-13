package com.example.transactionservice.vnpay;

import com.example.transactionservice.payment.MarketPaymentResponse;
import com.example.transactionservice.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayService {

    private final VNPayConfig config;

    public String createPaymentUrl(String txnRef, double amount, String orderInfo, String ipAddr) {
        try {
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", config.getVersion());
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", config.getTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf((long) (amount * 100)));
            vnpParams.put("vnp_CurrCode", config.getCurrCode());
            vnpParams.put("vnp_TxnRef", txnRef);
            vnpParams.put("vnp_OrderInfo", orderInfo);
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", config.getReturnUrl());
            vnpParams.put("vnp_IpAddr", ipAddr);

            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            vnpParams.put("vnp_CreateDate", now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

            LocalDateTime expire = now.plusMinutes(15);
            vnpParams.put("vnp_ExpireDate", expire.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

            // Build data theo ĐÚNG tài liệu VNPay
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (String fieldName : fieldNames) {
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    // Build hash data - ĐÚNG THEO TÀI LIỆU: US_ASCII
                    if (hashData.length() > 0) {
                        hashData.append('&');
                    }
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                    // Build query
                    if (query.length() > 0) {
                        query.append('&');
                    }
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                }
            }

            log.info("HashData before hash: {}", hashData.toString());

            String secureHash = hmacSHA512(config.getSecretKey(), hashData.toString());
            log.info("Generated SecureHash: {}", secureHash);

            String queryUrl = query.toString();
            queryUrl += "&vnp_SecureHash=" + secureHash;

            String paymentUrl = config.getApiUrl() + "?" + queryUrl;

            return paymentUrl;

        } catch (Exception e) {
            log.error("Error creating VNPay URL", e);
            throw new RuntimeException("Error while creating VNPay URL", e);
        }
    }

    private String hmacSHA512(String key, String data) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
        mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hash = new StringBuilder();
        for (byte b : bytes) {
            hash.append(String.format("%02x", b));
        }
        return hash.toString();
    }

    public MarketPaymentResponse pay(Transaction tx, String clientIp) {
        String paymentUrl = createPaymentUrl(
                tx.getId(),
                tx.getAmount(),
                "Thanh toan don hang " + tx.getId(),
                clientIp
        );

        tx.setPaymentUrl(paymentUrl);

        return MarketPaymentResponse.builder()
                .transactionId(tx.getId())
                .status("WAITING_PAYMENT")
                .paymentUrl(paymentUrl)
                .build();
    }
}