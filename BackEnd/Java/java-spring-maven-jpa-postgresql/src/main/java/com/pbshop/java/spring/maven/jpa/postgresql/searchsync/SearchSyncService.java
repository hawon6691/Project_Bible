package com.pbshop.java.spring.maven.jpa.postgresql.searchsync;

import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;

@Service
@Transactional
public class SearchSyncService {

    private final SearchIndexOutboxRepository searchIndexOutboxRepository;

    public SearchSyncService(SearchIndexOutboxRepository searchIndexOutboxRepository) {
        this.searchIndexOutboxRepository = searchIndexOutboxRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> summary(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        return Map.of(
                "total", searchIndexOutboxRepository.count(),
                "pending", searchIndexOutboxRepository.countByStatus("PENDING"),
                "failed", searchIndexOutboxRepository.countByStatus("FAILED"),
                "completed", searchIndexOutboxRepository.countByStatus("COMPLETED")
        );
    }

    public Map<String, Object> requeueFailed(AuthenticatedUserPrincipal principal, int limit) {
        requireAdmin(principal);
        var items = searchIndexOutboxRepository.findByStatusOrderByIdAsc("FAILED", PageRequest.of(0, limit));
        items.forEach(item -> {
            item.setStatus("PENDING");
            item.setRetryCount(item.getRetryCount() + 1);
        });
        searchIndexOutboxRepository.saveAll(items);
        return Map.of("requeuedCount", items.size());
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
