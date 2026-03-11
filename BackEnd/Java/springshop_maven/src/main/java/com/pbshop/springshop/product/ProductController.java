package com.pbshop.springshop.product;

import java.math.BigDecimal;
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

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;
import com.pbshop.springshop.product.dto.ProductDtos.SaveProductRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.success(productService.getProducts(categoryId, search, minPrice, maxPrice, sort, page, limit));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getProduct(@PathVariable Long id) {
        return ApiResponse.success(productService.getProduct(id));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> createProduct(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody SaveProductRequest request
    ) {
        return ApiResponse.success(productService.createProduct(principal, request));
    }

    @PatchMapping("/{id}")
    public ApiResponse<Map<String, Object>> updateProduct(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody SaveProductRequest request
    ) {
        return ApiResponse.success(productService.updateProduct(principal, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Object>> deleteProduct(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(productService.deleteProduct(principal, id));
    }
}
