package com.pbshop.java.spring.maven.jpa.postgresql.seller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;
import com.pbshop.java.spring.maven.jpa.postgresql.seller.dto.SellerDtos.SaveSellerRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/sellers")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getSellers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(sellerService.getSellers(page, limit, search));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getSeller(@PathVariable Long id) {
        return ApiResponse.success(sellerService.getSeller(id));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createSeller(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody SaveSellerRequest request
    ) {
        return ApiResponse.success(sellerService.createSeller(principal, request));
    }

    @PatchMapping("/{id}")
    public ApiResponse<Map<String, Object>> updateSeller(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody SaveSellerRequest request
    ) {
        return ApiResponse.success(sellerService.updateSeller(principal, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Object>> deleteSeller(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(sellerService.deleteSeller(principal, id));
    }
}
