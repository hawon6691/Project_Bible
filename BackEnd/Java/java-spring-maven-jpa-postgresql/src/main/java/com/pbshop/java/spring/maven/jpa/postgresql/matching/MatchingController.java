package com.pbshop.java.spring.maven.jpa.postgresql.matching;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;
import com.pbshop.java.spring.maven.jpa.postgresql.matching.dto.MatchingDtos.ApproveMappingRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.matching.dto.MatchingDtos.RejectMappingRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/matching")
public class MatchingController {
    private final MatchingService matchingService;
    public MatchingController(MatchingService matchingService) { this.matchingService = matchingService; }
    @GetMapping("/pending")
    public ApiResponse<Map<String, Object>> pending(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(matchingService.pending(principal));
    }
    @PostMapping("/{id}/approve")
    public ApiResponse<Map<String, Object>> approve(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id, @Valid @RequestBody ApproveMappingRequest request) {
        return ApiResponse.success(matchingService.approve(principal, id, request.productId()));
    }
    @PostMapping("/{id}/reject")
    public ApiResponse<Map<String, Object>> reject(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id, @Valid @RequestBody RejectMappingRequest request) {
        return ApiResponse.success(matchingService.reject(principal, id, request.reason()));
    }
    @PostMapping("/auto-match")
    public ApiResponse<Map<String, Object>> autoMatch(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(matchingService.autoMatch(principal));
    }
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(matchingService.stats(principal));
    }
}
