package com.pbshop.springshop.resilience;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;

@Service
public class ResilienceService {

    public Map<String, Object> list(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        return Map.of("items", items());
    }

    public Map<String, Object> policies(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        return Map.of(
                "items",
                List.of(Map.of(
                        "name", "crawler",
                        "options", Map.of("threshold", 5),
                        "stats", Map.of("opened", 1)
                ))
        );
    }

    public Map<String, Object> show(AuthenticatedUserPrincipal principal, String name) {
        requireAdmin(principal);
        return items().stream()
                .filter(item -> name.equals(item.get("name")))
                .findFirst()
                .orElse(Map.of("name", name, "state", "CLOSED", "failureCount", 0));
    }

    public Map<String, Object> reset(AuthenticatedUserPrincipal principal, String name) {
        requireAdmin(principal);
        return Map.of("message", "Circuit Breaker가 초기화되었습니다.", "name", name);
    }

    private List<Map<String, Object>> items() {
        return List.of(
                Map.of("name", "search-sync", "state", "CLOSED", "failureCount", 0),
                Map.of("name", "crawler", "state", "HALF_OPEN", "failureCount", 1)
        );
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
