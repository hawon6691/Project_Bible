package com.pbshop.java.spring.maven.jpa.postgresql.matching;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.product.Product;
import com.pbshop.java.spring.maven.jpa.postgresql.product.ProductRepository;

@Service
@Transactional
public class MatchingService {
    private final ProductMappingRepository productMappingRepository;
    private final ProductRepository productRepository;
    public MatchingService(ProductMappingRepository productMappingRepository, ProductRepository productRepository) {
        this.productMappingRepository = productMappingRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> pending(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        List<Map<String, Object>> items = productMappingRepository.findByStatusOrderByIdDesc("PENDING").stream().map(this::toResponse).toList();
        return Map.of("items", items, "pagination", Map.of("page", 1, "limit", items.size() == 0 ? 20 : items.size(), "total", items.size(), "totalPages", items.isEmpty() ? 0 : 1));
    }

    public Map<String, Object> approve(AuthenticatedUserPrincipal principal, Long id, Long productId) {
        requireAdmin(principal);
        ProductMapping mapping = requireMapping(id);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
        mapping.setProduct(product);
        mapping.setStatus("APPROVED");
        mapping.setReason(null);
        return Map.of("message", "매핑을 승인했습니다.");
    }

    public Map<String, Object> reject(AuthenticatedUserPrincipal principal, Long id, String reason) {
        requireAdmin(principal);
        ProductMapping mapping = requireMapping(id);
        mapping.setStatus("REJECTED");
        mapping.setReason(reason);
        return Map.of("message", "매핑을 거절했습니다.");
    }

    public Map<String, Object> autoMatch(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        int matched = 0;
        for (ProductMapping mapping : productMappingRepository.findByStatusOrderByIdDesc("PENDING")) {
            Product product = productRepository.findAll().stream()
                    .filter(item -> item.getName().contains(mapping.getSourceName()))
                    .findFirst().orElse(null);
            if (product != null) {
                mapping.setProduct(product);
                mapping.setStatus("APPROVED");
                mapping.setReason("AUTO_MATCHED");
                matched++;
            }
        }
        return Map.of("matchedCount", matched, "pendingCount", productMappingRepository.countByStatus("PENDING"));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> stats(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        return Map.of(
                "pending", productMappingRepository.countByStatus("PENDING"),
                "approved", productMappingRepository.countByStatus("APPROVED"),
                "rejected", productMappingRepository.countByStatus("REJECTED"));
    }

    private ProductMapping requireMapping(Long id) {
        return productMappingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "매핑 대상을 찾을 수 없습니다."));
    }

    private Map<String, Object> toResponse(ProductMapping mapping) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", mapping.getId());
        item.put("sourceName", mapping.getSourceName());
        item.put("productId", mapping.getProduct() == null ? null : mapping.getProduct().getId());
        item.put("status", mapping.getStatus());
        item.put("reason", mapping.getReason() == null ? "" : mapping.getReason());
        item.put("createdAt", mapping.getCreatedAt() == null ? null : mapping.getCreatedAt().toString());
        item.put("updatedAt", mapping.getUpdatedAt() == null ? null : mapping.getUpdatedAt().toString());
        return item;
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
