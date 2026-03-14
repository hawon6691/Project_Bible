package com.pbshop.springshop.pcbuilder;

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
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;
import com.pbshop.springshop.pcbuilder.dto.PcBuilderDtos.AddPartRequest;
import com.pbshop.springshop.pcbuilder.dto.PcBuilderDtos.CompatibilityRuleRequest;
import com.pbshop.springshop.pcbuilder.dto.PcBuilderDtos.CreateBuildRequest;
import com.pbshop.springshop.pcbuilder.dto.PcBuilderDtos.UpdateBuildRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}")
public class PcBuildController {
    private final PcBuilderService service;
    public PcBuildController(PcBuilderService service) { this.service = service; }
    @GetMapping("/pc-builds") public ApiResponse<Map<String, Object>> list(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) { return ApiResponse.success(service.listMine(principal)); }
    @PostMapping("/pc-builds") public ApiResponse<Map<String, Object>> create(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @Valid @RequestBody CreateBuildRequest request) { return ApiResponse.success(service.create(principal, request)); }
    @GetMapping("/pc-builds/{id}") public ApiResponse<Map<String, Object>> show(@PathVariable Long id) { return ApiResponse.success(service.show(id)); }
    @PatchMapping("/pc-builds/{id}") public ApiResponse<Map<String, Object>> update(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id, @Valid @RequestBody UpdateBuildRequest request) { return ApiResponse.success(service.update(principal, id, request)); }
    @DeleteMapping("/pc-builds/{id}") public ApiResponse<Map<String, Object>> delete(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id) { return ApiResponse.success(service.delete(principal, id)); }
    @PostMapping("/pc-builds/{id}/parts") public ApiResponse<Map<String, Object>> addPart(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id, @Valid @RequestBody AddPartRequest request) { return ApiResponse.success(service.addPart(principal, id, request)); }
    @DeleteMapping("/pc-builds/{id}/parts/{partId}") public ApiResponse<Map<String, Object>> removePart(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id, @PathVariable Long partId) { return ApiResponse.success(service.removePart(principal, id, partId)); }
    @GetMapping("/pc-builds/{id}/compatibility") public ApiResponse<Map<String, Object>> compatibility(@PathVariable Long id) { return ApiResponse.success(service.compatibility(id)); }
    @GetMapping("/pc-builds/{id}/share") public ApiResponse<Map<String, Object>> share(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id) { return ApiResponse.success(service.share(principal, id)); }
    @GetMapping("/pc-builds/shared/{shareCode}") public ApiResponse<Map<String, Object>> shared(@PathVariable String shareCode) { return ApiResponse.success(service.shared(shareCode)); }
    @GetMapping("/pc-builds/popular") public ApiResponse<Map<String, Object>> popular() { return ApiResponse.success(service.popular()); }
    @GetMapping("/admin/compatibility-rules") public ApiResponse<List<Map<String, Object>>> rules(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) { return ApiResponse.success(service.rules(principal)); }
    @PostMapping("/admin/compatibility-rules") public ApiResponse<Map<String, Object>> createRule(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @Valid @RequestBody CompatibilityRuleRequest request) { return ApiResponse.success(service.createRule(principal, request)); }
    @PatchMapping("/admin/compatibility-rules/{id}") public ApiResponse<Map<String, Object>> updateRule(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id, @Valid @RequestBody CompatibilityRuleRequest request) { return ApiResponse.success(service.updateRule(principal, id, request)); }
    @DeleteMapping("/admin/compatibility-rules/{id}") public ApiResponse<Map<String, Object>> deleteRule(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id) { return ApiResponse.success(service.deleteRule(principal, id)); }
}
