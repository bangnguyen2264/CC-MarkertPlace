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

import jakarta.servlet.http.HttpServletResponse;
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
     * Xử lý VNPay return URL - Update transaction status và redirect về frontend
     * (Cách 2: Backend xử lý callback và redirect về frontend)
     */
    @GetMapping("/vnpay-return")
    public void handleVNPayReturn(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception {
        try {
            // 1. Verify signature
            String vnpSecureHash = params.get("vnp_SecureHash");
            Map<String, String> signParams = new HashMap<>(params);
            signParams.remove("vnp_SecureHash");
            signParams.remove("vnp_SecureHashType");

            String calculatedHash = calculateSignature(signParams);

            if (!calculatedHash.equalsIgnoreCase(vnpSecureHash)) {
                log.error("❌ Invalid signature");
                String redirectUrl = buildCallbackUrl("failed", null, "Chữ ký không hợp lệ");
                response.sendRedirect(redirectUrl);
                return;
            }

            // 2. Lấy thông tin
            String responseCode = params.get("vnp_ResponseCode");
            String txnRef = params.get("vnp_TxnRef");
            String vnpayTransactionNo = params.get("vnp_TransactionNo");
            String bankCode = params.get("vnp_BankCode");
            String amount = params.get("vnp_Amount");
            String payDate = params.get("vnp_PayDate");

            log.info("📥 VNPay callback: txn={}, code={}", txnRef, responseCode);

            // 3. Get transaction
            Transaction transaction = transactionService.getById(txnRef);

            // 4. Update status - chỉ gọi update() một lần duy nhất
            String callbackStatus = "failed";
            String callbackMessage = "Giao dịch không thành công";

            if ("00".equals(responseCode)) {
                // Gọi update() sẽ xử lý toàn bộ logic: set status, paidAt, gọi walletIntegration.pay()
                transactionService.update(transaction.getId(), TransactionStatus.SUCCESS);

                callbackStatus = "success";
                callbackMessage = "Thanh toán thành công";
                log.info("✅ Payment SUCCESS: {}", txnRef);
            } else {
                transactionService.update(transaction.getId(), TransactionStatus.FAILED);

                callbackMessage = getVNPayErrorMessage(responseCode);
                log.warn("⚠️ Payment FAILED: {} - code: {}", txnRef, responseCode);
            }

            // 5. Redirect về frontend với tất cả thông tin
            String redirectUrl = buildCallbackUrl(
                callbackStatus,
                transaction.getId(),
                callbackMessage,
                amount,
                bankCode,
                payDate,
                vnpayTransactionNo
            );

            log.info("🔀 Redirecting to: {}", redirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("❌ Error processing callback", e);
            String redirectUrl = buildCallbackUrl("failed", null, "Lỗi xử lý giao dịch: " + e.getMessage());
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Xây dựng URL callback cho frontend (đầy đủ thông tin)
     */
    private String buildCallbackUrl(String status, String transactionId, String message,
                                    String amount, String bankCode, String payDate, String vnpTransactionNo) {
        // TODO: Cập nhật với domain frontend của bạn
        String frontendUrl = "http://localhost:5173/buyer/payment/callback";
        
        try {
            StringBuilder url = new StringBuilder(frontendUrl);
            url.append("?status=").append(status);
            url.append("&transactionId=").append(transactionId != null && !transactionId.isEmpty() ? transactionId : "");
            url.append("&message=").append(URLEncoder.encode(message, StandardCharsets.UTF_8));
            url.append("&amount=").append(amount != null ? amount : "");
            url.append("&bankCode=").append(bankCode != null ? bankCode : "");
            url.append("&payDate=").append(payDate != null ? payDate : "");
            url.append("&vnpTransactionNo=").append(vnpTransactionNo != null ? vnpTransactionNo : "");
            
            return url.toString();
        } catch (Exception e) {
            log.error("Error building callback URL", e);
            return frontendUrl + "?status=error&message=" + URLEncoder.encode("Lỗi tạo URL callback", StandardCharsets.UTF_8);
        }
    }

    /**
     * Xây dựng URL callback cho frontend (chỉ thông tin cơ bản)
     */
    private String buildCallbackUrl(String status, String transactionId, String message) {
        return buildCallbackUrl(status, transactionId, message, null, null, null, null);
    }

    /**
     * Lấy thông điệp lỗi VNPay theo mã lỗi
     */
    private String getVNPayErrorMessage(String responseCode) {
        Map<String, String> errorMessages = new HashMap<>();
        errorMessages.put("07", "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo)");
        errorMessages.put("09", "Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking");
        errorMessages.put("10", "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần");
        errorMessages.put("11", "Đã hết hạn chờ thanh toán");
        errorMessages.put("12", "Thẻ/Tài khoản bị khóa");
        errorMessages.put("13", "Nhập sai mật khẩu xác thực giao dịch (OTP)");
        errorMessages.put("24", "Khách hàng hủy giao dịch");
        errorMessages.put("51", "Tài khoản không đủ số dư");
        errorMessages.put("65", "Vượt quá hạn mức giao dịch trong ngày");
        errorMessages.put("75", "Ngân hàng đang bảo trì");
        errorMessages.put("79", "Nhập sai mật khẩu thanh toán quá số lần");
        errorMessages.put("99", "Các lỗi khác");

        return errorMessages.getOrDefault(responseCode, "Giao dịch không thành công (Mã: " + responseCode + ")");
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