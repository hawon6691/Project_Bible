package com.pbshop.springshop.payment;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;
import com.pbshop.springshop.payment.dto.PaymentDtos.CreatePaymentRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createPayment(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        return ApiResponse.success(paymentService.createPayment(principal, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getPayment(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(paymentService.getPayment(principal, id));
    }

    @PostMapping("/{id}/refund")
    public ApiResponse<Map<String, Object>> refundPayment(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(paymentService.refundPayment(principal, id));
    }
}
