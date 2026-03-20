package com.pbshop.java.spring.maven.jpa.postgresql.query;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.product.Product;
import com.pbshop.java.spring.maven.jpa.postgresql.product.ProductRepository;

@Service
@Transactional
public class QueryService {

    private final ProductRepository productRepository;
    private final ProductQueryViewRepository productQueryViewRepository;
    private final ObjectMapper objectMapper;

    public QueryService(
            ProductRepository productRepository,
            ProductQueryViewRepository productQueryViewRepository,
            ObjectMapper objectMapper
    ) {
        this.productRepository = productRepository;
        this.productQueryViewRepository = productQueryViewRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listProducts(int page, int limit) {
        var result = productRepository.findAll(PageRequest.of(Math.max(page - 1, 0), limit));
        return Map.of(
                "items", result.getContent().stream().map(product -> serialize(product, false)).toList(),
                "pagination", Map.of(
                        "page", result.getNumber() + 1,
                        "limit", result.getSize(),
                        "total", result.getTotalElements(),
                        "totalPages", result.getTotalPages()
                )
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> showProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
        return serialize(product, true);
    }

    public Map<String, Object> sync(AuthenticatedUserPrincipal principal, Long productId) {
        requireAdmin(principal);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
        ProductQueryView view = productQueryViewRepository.findByProductId(productId).orElseGet(ProductQueryView::new);
        view.setProduct(product);
        view.setViewCount(1);
        view.setSearchKeywordsJson(writeKeywords(List.of("manual-sync")));
        productQueryViewRepository.save(view);
        return Map.of("message", "Query view가 동기화되었습니다.", "productId", productId);
    }

    public Map<String, Object> rebuild(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        productRepository.findAll().forEach(product -> {
            ProductQueryView view = productQueryViewRepository.findByProductId(product.getId()).orElseGet(ProductQueryView::new);
            view.setProduct(product);
            view.setViewCount(1);
            view.setSearchKeywordsJson(writeKeywords(List.of("rebuild")));
            productQueryViewRepository.save(view);
        });
        return Map.of("message", "Query view 재구성이 완료되었습니다.", "count", productRepository.count());
    }

    private Map<String, Object> serialize(Product product, boolean detail) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", product.getId());
        payload.put("name", product.getName());
        payload.put("slug", product.getSlug());
        payload.put("status", product.getStatus());
        if (detail) {
            payload.put("specs", product.getSpecs().stream()
                    .map(spec -> Map.of("name", spec.getSpecKey(), "value", spec.getSpecValue()))
                    .toList());
            payload.put("priceEntries", product.getPriceEntries().stream()
                    .map(entry -> Map.of("price", entry.getPrice(), "sellerId", entry.getSeller().getId()))
                    .toList());
        }
        return payload;
    }

    private String writeKeywords(List<String> keywords) {
        try {
            return objectMapper.writeValueAsString(keywords);
        } catch (Exception exception) {
            return "[]";
        }
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
