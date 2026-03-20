package com.pbshop.java.spring.maven.jpa.postgresql.searchsync;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/search/admin/index/outbox")
public class SearchSyncController {

    private final SearchSyncService searchSyncService;

    public SearchSyncController(SearchSyncService searchSyncService) {
        this.searchSyncService = searchSyncService;
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(searchSyncService.summary(principal));
    }

    @PostMapping("/requeue-failed")
    public ApiResponse<Map<String, Object>> requeueFailed(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.success(searchSyncService.requeueFailed(principal, limit));
    }
}
