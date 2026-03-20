package com.pbshop.springshop.wishlist;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getWishlist(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.success(wishlistService.getWishlist(principal, page, limit));
    }

    @PostMapping("/{productId}")
    public ApiResponse<Map<String, Object>> toggleWishlist(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long productId
    ) {
        return ApiResponse.success(wishlistService.toggle(principal, productId));
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Map<String, Object>> removeWishlist(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long productId
    ) {
        return ApiResponse.success(wishlistService.remove(principal, productId));
    }
}
