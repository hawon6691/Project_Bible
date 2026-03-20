package com.pbshop.java.spring.maven.jpa.postgresql.trust;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.product.PriceEntryRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.product.Seller;
import com.pbshop.java.spring.maven.jpa.postgresql.product.SellerRepository;

@Service
@Transactional
public class TrustService {

    private final TrustScoreHistoryRepository trustScoreHistoryRepository;
    private final SellerRepository sellerRepository;
    private final PriceEntryRepository priceEntryRepository;
    private final ObjectMapper objectMapper;

    public TrustService(
            TrustScoreHistoryRepository trustScoreHistoryRepository,
            SellerRepository sellerRepository,
            PriceEntryRepository priceEntryRepository,
            ObjectMapper objectMapper
    ) {
        this.trustScoreHistoryRepository = trustScoreHistoryRepository;
        this.sellerRepository = sellerRepository;
        this.priceEntryRepository = priceEntryRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentScore(Long sellerId) {
        Seller seller = findSeller(sellerId);
        TrustScoreHistory history = trustScoreHistoryRepository.findFirstBySellerIdOrderByRecordedAtDescIdDesc(sellerId)
                .orElseGet(() -> createHistory(seller));
        return toResponse(seller, history);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getHistory(Long sellerId, Integer limit) {
        Seller seller = findSeller(sellerId);
        int size = limit == null || limit <= 0 ? 20 : Math.min(limit, 100);
        return trustScoreHistoryRepository.findBySellerIdOrderByRecordedAtDescIdDesc(sellerId).stream()
                .limit(size)
                .map(history -> toResponse(seller, history))
                .toList();
    }

    public Map<String, Object> recalculate(AuthenticatedUserPrincipal principal, Long sellerId) {
        requireAdmin(principal);
        Seller seller = findSeller(sellerId);
        TrustScoreHistory history = createHistory(seller);
        return toResponse(seller, history);
    }

    private TrustScoreHistory createHistory(Seller seller) {
        int listingCount = priceEntryRepository.findAll().stream()
                .map(entry -> entry.getSeller().getId())
                .filter(seller.getId()::equals)
                .toList()
                .size();
        int score = Math.min(100, 70 + Math.min(listingCount * 5, 25) + ("ACTIVE".equalsIgnoreCase(seller.getStatus()) ? 5 : -10));
        score = Math.max(0, score);

        Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("deliveryScore", score);
        breakdown.put("priceAccuracy", Math.max(score - 2, 0));
        breakdown.put("responseTime", 2.0);
        breakdown.put("reviewScore", Math.max(score / 20.0, 1.0));

        TrustScoreHistory history = new TrustScoreHistory();
        history.setSeller(seller);
        history.setScore(score);
        history.setGrade(toGrade(score));
        history.setTrend("STABLE");
        history.setBreakdownJson(writeJson(breakdown));
        history.setRecordedAt(OffsetDateTime.now());
        return trustScoreHistoryRepository.save(history);
    }

    private Seller findSeller(Long sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "판매처를 찾을 수 없습니다."));
    }

    private Map<String, Object> toResponse(Seller seller, TrustScoreHistory history) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sellerId", seller.getId());
        response.put("sellerName", seller.getName());
        response.put("overallScore", history.getScore());
        response.put("grade", history.getGrade());
        response.put("breakdown", readJsonMap(history.getBreakdownJson()));
        response.put("trend", history.getTrend());
        response.put("lastUpdatedAt", history.getRecordedAt() == null ? null : history.getRecordedAt().toString());
        return response;
    }

    private String toGrade(int score) {
        if (score >= 95) {
            return "A+";
        }
        if (score >= 90) {
            return "A";
        }
        if (score >= 80) {
            return "B+";
        }
        if (score >= 70) {
            return "B";
        }
        if (score >= 60) {
            return "C";
        }
        return "D";
    }

    private void requireAdmin(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }

    private String writeJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "신뢰도 세부 정보를 저장할 수 없습니다.");
        }
    }

    private Map<String, Object> readJsonMap(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "신뢰도 세부 정보를 읽을 수 없습니다.");
        }
    }
}
