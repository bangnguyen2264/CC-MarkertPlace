package com.example.transactionservice.payment;

import com.example.commondto.constant.TransactionStatus;
import com.example.transactionservice.transaction.Transaction;
import com.example.transactionservice.transaction.TransactionService;
import com.example.transactionservice.vnpay.VNPayConfig;
import com.example.transactionservice.vnpay.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final VNPayService vnPayService;
    private final VNPayConfig vnpayConfig;
    private final TransactionService transactionService;

    /**
     * Xử lý VNPay return URL - Update transaction status
     */
    @GetMapping("/vnpay-return")
    public ResponseEntity<Map<String, String>> handleVNPayReturn(@RequestParam Map<String, String> params) {
        Map<String, String> response = new HashMap<>();

        try {
            // 1. Verify signature
            String vnpSecureHash = params.get("vnp_SecureHash");
            Map<String, String> signParams = new HashMap<>(params);
            signParams.remove("vnp_SecureHash");
            signParams.remove("vnp_SecureHashType");

            String calculatedHash = calculateSignature(signParams);

            if (!calculatedHash.equalsIgnoreCase(vnpSecureHash)) {
                log.error("❌ Invalid signature");
                response.put("status", "error");
                response.put("message", "Invalid signature");
                return ResponseEntity.badRequest().body(response);
            }

            // 2. Lấy thông tin
            String responseCode = params.get("vnp_ResponseCode");
            String txnRef = params.get("vnp_TxnRef");
            String vnpayTransactionNo = params.get("vnp_TransactionNo");
            String bankCode = params.get("vnp_BankCode");

            log.info("📥 VNPay callback: txn={}, code={}", txnRef, responseCode);

            // 3. Get transaction
            Transaction transaction = transactionService.getById(txnRef);

            // 4. Update status
            if ("00".equals(responseCode)) {
                transaction.setStatus(TransactionStatus.SUCCESS);
                transaction.setPaidAt(LocalDateTime.now());


                transactionService.update(transaction.getId(), TransactionStatus.SUCCESS);

                log.info("✅ Payment SUCCESS: {}", txnRef);

                response.put("status", "success");
                response.put("message", "Payment successful");
            } else {
                transaction.setStatus(TransactionStatus.FAILED);
                transactionService.update(transaction.getId(), TransactionStatus.FAILED);

                log.warn("⚠️ Payment FAILED: {} - code: {}", txnRef, responseCode);

                response.put("status", "failed");
                response.put("message", "Payment failed");
            }

            response.put("transactionId", txnRef);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error processing callback", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private String calculateSignature(Map<String, String> params) throws Exception {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(fieldName)
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            }
        }

        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
        mac.init(new javax.crypto.spec.SecretKeySpec(
                vnpayConfig.getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        byte[] bytes = mac.doFinal(hashData.toString().getBytes(StandardCharsets.UTF_8));

        StringBuilder hash = new StringBuilder();
        for (byte b : bytes) {
            hash.append(String.format("%02x", b));
        }
        return hash.toString();
    }
}