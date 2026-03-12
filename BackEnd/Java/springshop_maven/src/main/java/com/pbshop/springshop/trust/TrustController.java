package com.pbshop.springshop.trust;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/trust")
public class TrustController {

    private final TrustService trustService;

    public TrustController(TrustService trustService) {
        this.trustService = trustService;
    }

    @GetMapping("/sellers/{sellerId}")
    public ApiResponse<Map<String, Object>> getSellerScore(@PathVariable Long sellerId) {
        return ApiResponse.success(trustService.getCurrentScore(sellerId));
    }

    @GetMapping("/sellers/{sellerId}/history")
    public ApiResponse<List<Map<String, Object>>> getHistory(
            @PathVariable Long sellerId,
            @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.success(trustService.getHistory(sellerId, limit));
    }

    @PostMapping("/admin/sellers/{sellerId}/recalculate")
    public ApiResponse<Map<String, Object>> recalculate(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long sellerId
    ) {
        return ApiResponse.success(trustService.recalculate(principal, sellerId));
    }
}
