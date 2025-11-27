package com.example.transactionservice.transaction;

import com.example.commondto.constant.PaymentMethod;
import com.example.commondto.constant.TransactionStatus;
import com.example.commondto.dto.request.MarketPurchaseMessage;
import com.example.transactionservice.payment.MarketPaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Tạo transaction mới")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Transaction> createTransaction(@Valid @ModelAttribute MarketPurchaseMessage message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createPendingTransaction(message));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Transaction>> getAll(@Valid @ModelAttribute @ParameterObject TransactionFilter filter) {
        return ResponseEntity.ok(transactionService.getAll(filter));
    }

    @GetMapping("{id}")
    public ResponseEntity<Transaction> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    @Operation(summary = "Cập nhật thông tin transaction")
    @PatchMapping("/{id}")
    public ResponseEntity<Transaction> update(@PathVariable("id") String id, @RequestParam("status") TransactionStatus status) {
        return ResponseEntity.ok(transactionService.update(id, status));
    }

    @PostMapping("/{transactionId}/pay")
    public ResponseEntity<MarketPaymentResponse> payTransaction(
            @PathVariable("transactionId") String transactionId,
            @RequestParam("paymentMethod") PaymentMethod paymentMethod,
            HttpServletRequest request
    ) {
        String clientIp = getClientIp(request);
        MarketPaymentResponse response = transactionService.pay(transactionId, paymentMethod, clientIp);
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method lấy IP thật
     */
    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0]; // nếu đi qua proxy/ngrok
        }
        return request.getRemoteAddr();
    }
}
