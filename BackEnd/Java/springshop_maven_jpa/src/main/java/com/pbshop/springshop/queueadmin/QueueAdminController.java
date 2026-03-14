package com.pbshop.springshop.queueadmin;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/admin/queues")
public class QueueAdminController {

    private final QueueAdminService queueAdminService;

    public QueueAdminController(QueueAdminService queueAdminService) {
        this.queueAdminService = queueAdminService;
    }

    @GetMapping("/supported")
    public ApiResponse<Map<String, Object>> supported(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(queueAdminService.supported(principal));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(queueAdminService.stats(principal));
    }

    @PostMapping("/auto-retry")
    public ApiResponse<Map<String, Object>> autoRetry(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "1") int perQueueLimit,
            @RequestParam(defaultValue = "1") int maxTotal
    ) {
        return ApiResponse.success(queueAdminService.autoRetry(principal, perQueueLimit, maxTotal));
    }

    @GetMapping("/{queueName}/failed")
    public ApiResponse<Map<String, Object>> failed(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable String queueName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.success(queueAdminService.failed(principal, queueName, page, limit));
    }

    @PostMapping("/{queueName}/failed/retry")
    public ApiResponse<Map<String, Object>> retryFailed(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable String queueName,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.success(queueAdminService.retryFailed(principal, queueName, limit));
    }

    @PostMapping("/{queueName}/jobs/{jobId}/retry")
    public ApiResponse<Map<String, Object>> retryJob(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable String queueName,
            @PathVariable String jobId
    ) {
        return ApiResponse.success(queueAdminService.retryJob(principal, queueName, jobId));
    }

    @DeleteMapping("/{queueName}/jobs/{jobId}")
    public ApiResponse<Map<String, Object>> removeJob(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable String queueName,
            @PathVariable String jobId
    ) {
        return ApiResponse.success(queueAdminService.removeJob(principal, queueName, jobId));
    }
}
