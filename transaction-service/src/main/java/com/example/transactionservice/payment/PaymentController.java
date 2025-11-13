package com.example.transactionservice.payment;

import com.example.transactionservice.vnpay.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;

    @PostMapping("/create")
    public String createPayment(HttpServletRequest request,
                                @RequestParam double amount,
                                @RequestParam String orderInfo) {
        String ipAddress = request.getRemoteAddr();
        String txnRef = UUID.randomUUID().toString().substring(0, 8);
        return vnPayService.createPaymentUrl(txnRef, amount, orderInfo, ipAddress);
    }
}