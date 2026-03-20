package com.pbshop.java.spring.maven.jpa.postgresql.deal;

import java.util.List;
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
import com.pbshop.java.spring.maven.jpa.postgresql.deal.dto.DealDtos.SaveDealRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.deal.dto.DealDtos.UpdateDealRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}")
public class DealController {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @GetMapping("/deals")
    public ApiResponse<List<Map<String, Object>>> getDeals(@RequestParam(required = false) String type) {
        return ApiResponse.success(dealService.getDeals(type));
    }

    @GetMapping("/deals/{id}")
    public ApiResponse<Map<String, Object>> getDeal(@PathVariable Long id) {
        return ApiResponse.success(dealService.getDeal(id));
    }

    @GetMapping("/admin/deals")
    public ApiResponse<List<Map<String, Object>>> getAdminDeals(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ApiResponse.success(dealService.getAdminDeals(principal));
    }

    @PostMapping("/deals/admin")
    public ApiResponse<Map<String, Object>> createDeal(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody SaveDealRequest request
    ) {
        return ApiResponse.success(dealService.createDeal(principal, request));
    }

    @PatchMapping("/deals/admin/{id}")
    public ApiResponse<Map<String, Object>> updateDeal(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateDealRequest request
    ) {
        return ApiResponse.success(dealService.updateDeal(principal, id, request));
    }

    @DeleteMapping("/deals/admin/{id}")
    public ApiResponse<Map<String, Object>> deleteDeal(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(dealService.deleteDeal(principal, id));
    }
}
