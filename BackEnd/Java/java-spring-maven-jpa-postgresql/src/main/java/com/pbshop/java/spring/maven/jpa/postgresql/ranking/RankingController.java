package com.pbshop.java.spring.maven.jpa.postgresql.ranking;

import java.util.List;
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
@RequestMapping("${pbshop.api.base-path:/api/v1}/rankings")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/products/popular")
    public ApiResponse<List<Map<String, Object>>> getPopularProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.success(rankingService.getPopularProducts(categoryId, limit));
    }

    @GetMapping("/keywords/popular")
    public ApiResponse<List<Map<String, Object>>> getPopularKeywords(
            @RequestParam(required = false) Integer limit
    ) {
        return ApiResponse.success(rankingService.getPopularKeywords(limit));
    }

    @PostMapping("/admin/recalculate")
    public ApiResponse<Map<String, Object>> recalculate(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ApiResponse.success(rankingService.recalculate(principal));
    }
}
