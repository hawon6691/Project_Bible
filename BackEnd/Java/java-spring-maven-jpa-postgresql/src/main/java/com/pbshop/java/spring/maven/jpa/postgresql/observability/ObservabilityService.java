package com.pbshop.java.spring.maven.jpa.postgresql.observability;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;

@Service
public class ObservabilityService {

    public Map<String, Object> metrics(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        return Map.of(
                "totalRequests", 120,
                "errorRate", 0.8,
                "avgLatencyMs", 34,
                "p95LatencyMs", 90,
                "p99LatencyMs", 140,
                "statusBuckets", Map.of("2xx", 118, "5xx", 2)
        );
    }

    public Map<String, Object> traces(AuthenticatedUserPrincipal principal, int limit, String pathContains) {
        requireAdmin(principal);
        return Map.of(
                "items",
                List.of(Map.of(
                        "requestId", "req-1",
                        "method", "GET",
                        "path", pathContains == null || pathContains.isBlank() ? "/api/v1/health" : pathContains,
                        "statusCode", 200,
                        "durationMs", 12
                ))
        );
    }

    public Map<String, Object> dashboard(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("process", Map.of("uptimeSec", 3600));
        payload.put("metrics", metrics(principal));
        payload.put("queue", Map.of("healthy", true));
        payload.put("resilience", Map.of("healthy", true));
        payload.put("searchSync", Map.of("healthy", false));
        payload.put("crawler", Map.of("healthy", true));
        payload.put("opsSummary", Map.of("overallStatus", "degraded"));
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
