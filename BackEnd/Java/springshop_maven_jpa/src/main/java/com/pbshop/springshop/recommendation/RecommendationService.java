package com.pbshop.springshop.recommendation;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.product.Product;
import com.pbshop.springshop.product.ProductRepository;
import com.pbshop.springshop.recommendation.dto.RecommendationDtos.SaveRecommendationRequest;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@Service
@Transactional
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public RecommendationService(
            RecommendationRepository recommendationRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.recommendationRepository = recommendationRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTrendingRecommendations(Integer limit) {
        int size = normalizeLimit(limit);
        List<Map<String, Object>> items = recommendationRepository.findByActiveTrueAndTargetTypeOrderByScoreDescIdDesc("TRENDING").stream()
                .limit(size)
                .map(this::toRecommendationItem)
                .toList();
        return Map.of("source", "TRENDING", "items", items);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPersonalRecommendations(AuthenticatedUserPrincipal principal, Integer limit) {
        User user = requireUser(principal);
        int size = normalizeLimit(limit);
        List<Map<String, Object>> directItems = recommendationRepository.findByActiveTrueAndTargetUserIdOrderByScoreDescIdDesc(user.getId()).stream()
                .limit(size)
                .map(this::toRecommendationItem)
                .toList();
        if (!directItems.isEmpty()) {
            return Map.of("source", "PERSONALIZED", "items", directItems);
        }
        List<Map<String, Object>> fallbackItems = recommendationRepository.findByActiveTrueAndTargetTypeOrderByScoreDescIdDesc("TRENDING").stream()
                .limit(size)
                .map(this::toRecommendationItem)
                .toList();
        return Map.of("source", "FALLBACK_TRENDING", "items", fallbackItems);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAdminRecommendations(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        return recommendationRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(this::toRecommendationAdminResponse)
                .toList();
    }

    public Map<String, Object> createRecommendation(
            AuthenticatedUserPrincipal principal,
            SaveRecommendationRequest request
    ) {
        requireAdmin(principal);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));

        Recommendation recommendation = new Recommendation();
        recommendation.setProduct(product);
        recommendation.setTargetType(
                request.targetType() == null || request.targetType().isBlank()
                        ? "TRENDING"
                        : request.targetType().toUpperCase()
        );
        recommendation.setReason(request.reason());
        recommendation.setScore(request.score() == null ? BigDecimal.ZERO : request.score());
        recommendation.setActive(true);
        if (request.targetUserId() != null) {
            recommendation.setTargetUser(userRepository.findById(request.targetUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "대상 사용자를 찾을 수 없습니다.")));
        }
        return toRecommendationAdminResponse(recommendationRepository.save(recommendation));
    }

    public Map<String, Object> deleteRecommendation(AuthenticatedUserPrincipal principal, Long id) {
        requireAdmin(principal);
        Recommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "추천 정보를 찾을 수 없습니다."));
        recommendationRepository.delete(recommendation);
        return Map.of("message", "추천이 삭제되었습니다.");
    }

    private Map<String, Object> toRecommendationItem(Recommendation recommendation) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", recommendation.getId());
        response.put("reason", recommendation.getReason() == null ? "" : recommendation.getReason());
        response.put("score", recommendation.getScore());
        response.put("product", Map.of(
                "id", recommendation.getProduct().getId(),
                "name", recommendation.getProduct().getName(),
                "thumbnailUrl", recommendation.getProduct().getThumbnailUrl() == null ? "" : recommendation.getProduct().getThumbnailUrl()
        ));
        return response;
    }

    private Map<String, Object> toRecommendationAdminResponse(Recommendation recommendation) {
        Map<String, Object> response = new LinkedHashMap<>(toRecommendationItem(recommendation));
        response.put("targetType", recommendation.getTargetType());
        response.put("targetUserId", recommendation.getTargetUser() == null ? null : recommendation.getTargetUser().getId());
        response.put("active", recommendation.isActive());
        return response;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 10;
        }
        return Math.min(limit, 50);
    }

    private User requireUser(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return userRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "인증 사용자를 찾을 수 없습니다."));
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
