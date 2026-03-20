package com.pbshop.java.spring.maven.jpa.postgresql.order;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;
import com.pbshop.java.spring.maven.jpa.postgresql.order.dto.OrderDtos.CreateOrderRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.order.dto.OrderDtos.UpdateOrderStatusRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public ApiResponse<Map<String, Object>> createOrder(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ApiResponse.success(orderService.createOrder(principal, request));
    }

    @GetMapping("/orders")
    public ApiResponse<Map<String, Object>> getOrders(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.success(orderService.getOrders(principal, page, limit));
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<Map<String, Object>> getOrderDetail(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(orderService.getOrderDetail(principal, id));
    }

    @PostMapping("/orders/{id}/cancel")
    public ApiResponse<Map<String, Object>> cancelOrder(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(orderService.cancelOrder(principal, id));
    }

    @GetMapping("/admin/orders")
    public ApiResponse<Map<String, Object>> getAdminOrders(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(orderService.getAdminOrders(principal, page, limit, status));
    }

    @PatchMapping("/admin/orders/{id}/status")
    public ApiResponse<Map<String, Object>> updateOrderStatus(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return ApiResponse.success(orderService.updateOrderStatus(principal, id, request));
    }
}
