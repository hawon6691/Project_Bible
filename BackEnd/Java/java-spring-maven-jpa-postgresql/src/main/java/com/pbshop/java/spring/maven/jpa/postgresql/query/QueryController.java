package com.pbshop.java.spring.maven.jpa.postgresql.query;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}")
public class QueryController {

    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/query/products")
    public ApiResponse<Map<String, Object>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.success(queryService.listProducts(page, limit));
    }

    @GetMapping("/query/products/{productId}")
    public ApiResponse<Map<String, Object>> showProduct(@PathVariable Long productId) {
        return ApiResponse.success(queryService.showProduct(productId));
    }

    @PostMapping("/admin/query/products/rebuild")
    public ApiResponse<Map<String, Object>> rebuild(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(queryService.rebuild(principal));
    }

    @PostMapping("/admin/query/products/{productId}/sync")
    public ApiResponse<Map<String, Object>> sync(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long productId
    ) {
        return ApiResponse.success(queryService.sync(principal, productId));
    }
}
