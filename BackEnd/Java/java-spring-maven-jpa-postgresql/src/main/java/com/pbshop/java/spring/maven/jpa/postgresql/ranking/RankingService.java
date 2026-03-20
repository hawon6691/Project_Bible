package com.pbshop.java.spring.maven.jpa.postgresql.ranking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.activity.RecentProductView;
import com.pbshop.java.spring.maven.jpa.postgresql.activity.RecentProductViewRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.activity.SearchHistory;
import com.pbshop.java.spring.maven.jpa.postgresql.activity.SearchHistoryRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.product.PriceEntry;

@Service
@Transactional(readOnly = true)
public class RankingService {

    private final RecentProductViewRepository recentProductViewRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    public RankingService(
            RecentProductViewRepository recentProductViewRepository,
            SearchHistoryRepository searchHistoryRepository
    ) {
        this.recentProductViewRepository = recentProductViewRepository;
        this.searchHistoryRepository = searchHistoryRepository;
    }

    public List<Map<String, Object>> getPopularProducts(Long categoryId, Integer limit) {
        int size = normalizeLimit(limit);
        AtomicInteger rank = new AtomicInteger(1);

        return recentProductViewRepository.findAll().stream()
                .filter(view -> categoryId == null || view.getProduct().getCategory().getId().equals(categoryId))
                .collect(Collectors.groupingBy(RecentProductView::getProduct, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted((left, right) -> Long.compare(right.getValue(), left.getValue()))
                .limit(size)
                .map(entry -> {
                    List<PriceEntry> prices = entry.getKey().getPriceEntries();
                    BigDecimal lowestPrice = prices.stream()
                            .map(PriceEntry::getPrice)
                            .min(BigDecimal::compareTo)
                            .orElse(BigDecimal.ZERO);
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("rank", rank.getAndIncrement());
                    response.put("rankChange", 0);
                    response.put("product", Map.of(
                            "id", entry.getKey().getId(),
                            "name", entry.getKey().getName(),
                            "lowestPrice", lowestPrice.setScale(2, RoundingMode.HALF_UP),
                            "thumbnailUrl", entry.getKey().getThumbnailUrl() == null ? "" : entry.getKey().getThumbnailUrl()
                    ));
                    response.put("score", entry.getValue());
                    return response;
                })
                .toList();
    }

    public List<Map<String, Object>> getPopularKeywords(Integer limit) {
        int size = normalizeLimit(limit);
        AtomicInteger rank = new AtomicInteger(1);

        return searchHistoryRepository.findAll().stream()
                .collect(Collectors.groupingBy(SearchHistory::getKeyword, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted((left, right) -> Long.compare(right.getValue(), left.getValue()))
                .limit(size)
                .map(entry -> {
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("rank", rank.getAndIncrement());
                    response.put("keyword", entry.getKey());
                    response.put("searchCount", entry.getValue());
                    response.put("rankChange", 0);
                    return response;
                })
                .toList();
    }

    public Map<String, Object> recalculate(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        long productCount = recentProductViewRepository.findAll().stream()
                .map(RecentProductView::getProduct)
                .map(product -> product.getId())
                .distinct()
                .count();
        long keywordCount = searchHistoryRepository.findAll().stream()
                .map(SearchHistory::getKeyword)
                .distinct()
                .count();
        return Map.of("updatedCount", productCount + keywordCount);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 20;
        }
        return Math.min(limit, 100);
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
