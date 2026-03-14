package com.pbshop.springshop.shortform;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;
import com.pbshop.springshop.shortform.dto.ShortformDtos.CommentRequest;
import com.pbshop.springshop.shortform.dto.ShortformDtos.CreateShortformRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/shortforms")
public class ShortformController {
    private final ShortformService shortformService;
    public ShortformController(ShortformService shortformService) { this.shortformService = shortformService; }
    @PostMapping public ApiResponse<Map<String, Object>> create(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @Valid @RequestBody CreateShortformRequest request) { return ApiResponse.success(shortformService.create(principal, request)); }
    @GetMapping public ApiResponse<Map<String, Object>> feed() { return ApiResponse.success(shortformService.feed()); }
    @GetMapping("/{id}") public ApiResponse<Map<String, Object>> show(@PathVariable Long id) { return ApiResponse.success(shortformService.show(id)); }
    @PostMapping("/{id}/likes/toggle") public ApiResponse<Map<String, Object>> toggleLike(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id) { return ApiResponse.success(shortformService.toggleLike(principal, id)); }
    @PostMapping("/{id}/comments") public ApiResponse<Map<String, Object>> addComment(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id, @Valid @RequestBody CommentRequest request) { return ApiResponse.success(shortformService.addComment(principal, id, request)); }
    @GetMapping("/{id}/comments") public ApiResponse<Map<String, Object>> comments(@PathVariable Long id) { return ApiResponse.success(shortformService.comments(id)); }
    @GetMapping("/ranking") public ApiResponse<List<Map<String, Object>>> ranking(@RequestParam(defaultValue = "daily") String period) { return ApiResponse.success(shortformService.ranking(period)); }
    @GetMapping("/{id}/transcode-status") public ApiResponse<Map<String, Object>> transcodeStatus(@PathVariable Long id) { return ApiResponse.success(shortformService.transcodeStatus(id)); }
    @PostMapping("/{id}/retry-transcode") public ApiResponse<Map<String, Object>> retry(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id) { return ApiResponse.success(shortformService.retry(principal, id)); }
    @DeleteMapping("/{id}") public ApiResponse<Map<String, Object>> delete(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id) { return ApiResponse.success(shortformService.delete(principal, id)); }
    @GetMapping("/users/{userId}") public ApiResponse<Map<String, Object>> byUser(@PathVariable Long userId) { return ApiResponse.success(shortformService.byUser(userId)); }
}
