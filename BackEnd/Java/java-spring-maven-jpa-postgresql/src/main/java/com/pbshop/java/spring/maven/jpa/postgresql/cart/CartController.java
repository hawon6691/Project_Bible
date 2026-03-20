package com.pbshop.java.spring.maven.jpa.postgresql.cart;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.cart.dto.CartDtos.StoreCartItemRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.cart.dto.CartDtos.UpdateCartItemRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getCart(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(cartService.getCart(principal));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> addToCart(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody StoreCartItemRequest request
    ) {
        return ApiResponse.success(cartService.addToCart(principal, request));
    }

    @PatchMapping("/{itemId}")
    public ApiResponse<Map<String, Object>> updateCartItem(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return ApiResponse.success(cartService.updateCartItem(principal, itemId, request));
    }

    @DeleteMapping("/{itemId}")
    public ApiResponse<Map<String, Object>> deleteCartItem(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long itemId
    ) {
        return ApiResponse.success(cartService.deleteCartItem(principal, itemId));
    }

    @DeleteMapping
    public ApiResponse<Map<String, Object>> clearCart(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(cartService.clearCart(principal));
    }
}
