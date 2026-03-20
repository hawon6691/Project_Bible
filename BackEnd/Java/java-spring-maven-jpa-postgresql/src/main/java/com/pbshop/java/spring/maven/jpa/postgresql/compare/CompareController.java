package com.pbshop.java.spring.maven.jpa.postgresql.compare;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;
import com.pbshop.java.spring.maven.jpa.postgresql.compare.dto.CompareDtos.AddCompareItemRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/compare")
public class CompareController {

    private final CompareService compareService;

    public CompareController(CompareService compareService) {
        this.compareService = compareService;
    }

    @PostMapping("/add")
    public ApiResponse<Map<String, Object>> add(
            @RequestHeader(value = "X-Compare-Key", defaultValue = "guest") String compareKey,
            @Valid @RequestBody AddCompareItemRequest request
    ) {
        return ApiResponse.success(compareService.add(compareKey, request.productId()));
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Map<String, Object>> remove(
            @RequestHeader(value = "X-Compare-Key", defaultValue = "guest") String compareKey,
            @PathVariable Long productId
    ) {
        return ApiResponse.success(compareService.remove(compareKey, productId));
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> index(
            @RequestHeader(value = "X-Compare-Key", defaultValue = "guest") String compareKey
    ) {
        return ApiResponse.success(compareService.list(compareKey));
    }

    @GetMapping("/detail")
    public ApiResponse<Map<String, Object>> detail(
            @RequestHeader(value = "X-Compare-Key", defaultValue = "guest") String compareKey
    ) {
        return ApiResponse.success(compareService.detail(compareKey));
    }
}
