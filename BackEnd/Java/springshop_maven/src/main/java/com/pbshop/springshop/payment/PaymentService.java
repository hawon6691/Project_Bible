package com.pbshop.springshop.payment;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.order.Order;
import com.pbshop.springshop.order.OrderRepository;
import com.pbshop.springshop.payment.dto.PaymentDtos.CreatePaymentRequest;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            UserRepository userRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> createPayment(AuthenticatedUserPrincipal principal, CreatePaymentRequest request) {
        User user = requireUser(principal);
        Order order = orderRepository.findDetailByIdAndUserId(request.orderId(), user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "주문을 찾을 수 없습니다."));

        if ("CANCELED".equalsIgnoreCase(order.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "취소된 주문은 결제할 수 없습니다.");
        }
        if ("PAID".equalsIgnoreCase(order.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미 결제된 주문입니다.");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setUser(user);
        payment.setMethod(request.method());
        payment.setAmount(order.getFinalAmount());
        payment.setStatus("PAID");
        payment.setProvider("mock-pg");
        payment.setProviderTransactionId("PG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setApprovedAt(OffsetDateTime.now());
        Payment saved = paymentRepository.save(payment);

        order.setStatus("PAID");
        orderRepository.save(order);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPayment(AuthenticatedUserPrincipal principal, Long paymentId) {
        User user = requireUser(principal);
        Payment payment = paymentRepository.findDetailByIdAndUserId(paymentId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));
        return toResponse(payment);
    }

    public Map<String, Object> refundPayment(AuthenticatedUserPrincipal principal, Long paymentId) {
        User user = requireUser(principal);
        Payment payment = paymentRepository.findDetailByIdAndUserId(paymentId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));

        if (!"PAID".equalsIgnoreCase(payment.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "결제 완료 상태만 환불할 수 있습니다.");
        }

        payment.setStatus("REFUNDED");
        payment.setRefundedAt(OffsetDateTime.now());
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        order.setStatus("REFUNDED");
        orderRepository.save(order);

        return toResponse(payment);
    }

    private Map<String, Object> toResponse(Payment payment) {
        return Map.of(
                "id", payment.getId(),
                "orderId", payment.getOrder().getId(),
                "status", payment.getStatus(),
                "method", payment.getMethod(),
                "amount", payment.getAmount(),
                "provider", payment.getProvider(),
                "providerTransactionId", payment.getProviderTransactionId() == null ? "" : payment.getProviderTransactionId(),
                "approvedAt", payment.getApprovedAt() == null ? "" : payment.getApprovedAt().toString(),
                "refundedAt", payment.getRefundedAt() == null ? "" : payment.getRefundedAt().toString()
        );
    }

    private User requireUser(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return userRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "인증 사용자를 찾을 수 없습니다."));
    }
}
