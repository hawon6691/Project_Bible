package com.pbshop.java.spring.maven.jpa.postgresql.activity;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.activity.dto.ActivityDtos.CreateSearchRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.product.Product;
import com.pbshop.java.spring.maven.jpa.postgresql.product.ProductRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.user.User;
import com.pbshop.java.spring.maven.jpa.postgresql.user.UserRepository;

@Service
@Transactional(readOnly = true)
public class ActivityService {

    private final RecentProductViewRepository recentProductViewRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ActivityService(
            RecentProductViewRepository recentProductViewRepository,
            SearchHistoryRepository searchHistoryRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.recentProductViewRepository = recentProductViewRepository;
        this.searchHistoryRepository = searchHistoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getActivities(AuthenticatedUserPrincipal principal) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("recentProductCount", recentProductViewRepository.countByUserId(principal.userId()));
        response.put("searchCount", searchHistoryRepository.countByUserId(principal.userId()));
        return response;
    }

    public List<Map<String, Object>> getRecentProducts(AuthenticatedUserPrincipal principal) {
        return recentProductViewRepository.findByUserIdOrderByViewedAtDesc(principal.userId()).stream()
                .map(this::toRecentProductResponse)
                .toList();
    }

    @Transactional
    public Map<String, Object> addRecentProduct(AuthenticatedUserPrincipal principal, Long productId) {
        RecentProductView view = recentProductViewRepository.findByUserIdAndProductId(principal.userId(), productId)
                .orElseGet(RecentProductView::new);
        view.setUser(getUser(principal.userId()));
        view.setProduct(getProduct(productId));
        view.setViewedAt(OffsetDateTime.now());
        return toRecentProductResponse(recentProductViewRepository.save(view));
    }

    public List<Map<String, Object>> getSearches(AuthenticatedUserPrincipal principal) {
        return searchHistoryRepository.findByUserIdOrderByIdDesc(principal.userId()).stream()
                .map(this::toSearchResponse)
                .toList();
    }

    @Transactional
    public Map<String, Object> createSearch(AuthenticatedUserPrincipal principal, CreateSearchRequest request) {
        SearchHistory history = new SearchHistory();
        history.setUser(getUser(principal.userId()));
        history.setKeyword(request.keyword());
        return toSearchResponse(searchHistoryRepository.save(history));
    }

    @Transactional
    public Map<String, Object> deleteSearch(AuthenticatedUserPrincipal principal, Long searchId) {
        SearchHistory history = searchHistoryRepository.findById(searchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "검색 기록을 찾을 수 없습니다."));
        if (!history.getUser().getId().equals(principal.userId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        searchHistoryRepository.delete(history);
        return Map.of("message", "검색 기록이 삭제되었습니다.");
    }

    @Transactional
    public Map<String, Object> clearSearches(AuthenticatedUserPrincipal principal) {
        searchHistoryRepository.deleteByUserId(principal.userId());
        return Map.of("message", "검색 기록이 모두 삭제되었습니다.");
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }

    private Map<String, Object> toRecentProductResponse(RecentProductView view) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", view.getId());
        response.put("productId", view.getProduct().getId());
        response.put("productName", view.getProduct().getName());
        response.put("categoryId", view.getProduct().getCategory().getId());
        response.put("viewedAt", view.getViewedAt());
        return response;
    }

    private Map<String, Object> toSearchResponse(SearchHistory history) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", history.getId());
        response.put("keyword", history.getKeyword());
        response.put("createdAt", history.getCreatedAt());
        return response;
    }
}
