package com.pbshop.springshop.spec;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;
import com.pbshop.springshop.spec.dto.SpecDtos.SaveSpecDefinitionRequest;
import com.pbshop.springshop.spec.dto.SpecDtos.SetProductSpecsRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}")
public class SpecController {

    private final SpecService specService;

    public SpecController(SpecService specService) {
        this.specService = specService;
    }

    @GetMapping("/specs/definitions")
    public ApiResponse<List<Map<String, Object>>> getDefinitions(@RequestParam(required = false) Long categoryId) {
        return ApiResponse.success(specService.getDefinitions(categoryId));
    }

    @PostMapping("/specs/definitions")
    public ApiResponse<Map<String, Object>> createDefinition(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody SaveSpecDefinitionRequest request
    ) {
        return ApiResponse.success(specService.createDefinition(principal, request));
    }

    @PatchMapping("/specs/definitions/{id}")
    public ApiResponse<Map<String, Object>> updateDefinition(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody SaveSpecDefinitionRequest request
    ) {
        return ApiResponse.success(specService.updateDefinition(principal, id, request));
    }

    @DeleteMapping("/specs/definitions/{id}")
    public ApiResponse<Map<String, Object>> deleteDefinition(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(specService.deleteDefinition(principal, id));
    }

    @GetMapping("/products/{productId}/specs")
    public ApiResponse<List<Map<String, Object>>> getProductSpecs(@PathVariable Long productId) {
        return ApiResponse.success(specService.getProductSpecs(productId));
    }

    @PutMapping("/products/{productId}/specs")
    public ApiResponse<List<Map<String, Object>>> setProductSpecs(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long productId,
            @Valid @RequestBody SetProductSpecsRequest request
    ) {
        return ApiResponse.success(specService.setProductSpecs(principal, productId, request));
    }
}
