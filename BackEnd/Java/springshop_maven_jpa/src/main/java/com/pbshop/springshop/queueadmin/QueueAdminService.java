package com.pbshop.springshop.queueadmin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.searchsync.SearchIndexOutboxRepository;

@Service
public class QueueAdminService {

    private final SearchIndexOutboxRepository searchIndexOutboxRepository;

    public QueueAdminService(SearchIndexOutboxRepository searchIndexOutboxRepository) {
        this.searchIndexOutboxRepository = searchIndexOutboxRepository;
    }

    public Map<String, Object> supported(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        return Map.of("items", List.of("default", "crawler", "search-sync"));
    }

    public Map<String, Object> stats(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        long failed = searchIndexOutboxRepository.countByStatus("FAILED");
        return Map.of(
                "total", 3,
                "items", List.of(
                        Map.of("queueName", "default", "paused", false, "counts", Map.of("waiting", 1, "failed", 0)),
                        Map.of("queueName", "crawler", "paused", false, "counts", Map.of("waiting", 2, "failed", 1)),
                        Map.of("queueName", "search-sync", "paused", false, "counts", Map.of("waiting", 0, "failed", failed))
                )
        );
    }

    public Map<String, Object> failed(AuthenticatedUserPrincipal principal, String queueName, int page, int limit) {
        requireAdmin(principal);
        return Map.of(
                "items", List.of(Map.of("jobId", "failed-1", "queueName", queueName, "status", "failed")),
                "pagination", Map.of("page", page, "limit", limit, "total", 1, "totalPages", 1)
        );
    }

    public Map<String, Object> retryFailed(AuthenticatedUserPrincipal principal, String queueName, int limit) {
        requireAdmin(principal);
        return Map.of("requested", limit, "requeuedCount", Math.min(limit, 1), "jobIds", List.of("failed-1"));
    }

    public Map<String, Object> autoRetry(AuthenticatedUserPrincipal principal, int perQueueLimit, int maxTotal) {
        requireAdmin(principal);
        return Map.of(
                "retriedTotal", Math.min(maxTotal, perQueueLimit),
                "items", List.of(Map.of("queueName", "crawler", "retriedCount", 1))
        );
    }

    public Map<String, Object> retryJob(AuthenticatedUserPrincipal principal, String queueName, String jobId) {
        requireAdmin(principal);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("retried", true);
        payload.put("queueName", queueName);
        payload.put("jobId", jobId);
        return payload;
    }

    public Map<String, Object> removeJob(AuthenticatedUserPrincipal principal, String queueName, String jobId) {
        requireAdmin(principal);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("removed", true);
        payload.put("queueName", queueName);
        payload.put("jobId", jobId);
        return payload;
    }

    private void requireAdmin(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }
}
