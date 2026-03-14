package com.pbshop.springshop.deal;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.deal.dto.DealDtos.SaveDealRequest;
import com.pbshop.springshop.deal.dto.DealDtos.UpdateDealRequest;
import com.pbshop.springshop.product.Product;
import com.pbshop.springshop.product.ProductRepository;

@Service
@Transactional
public class DealService {

    private final DealRepository dealRepository;
    private final ProductRepository productRepository;

    public DealService(DealRepository dealRepository, ProductRepository productRepository) {
        this.dealRepository = dealRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDeals(String type) {
        return dealRepository.findActiveDeals(OffsetDateTime.now(), type).stream()
                .map(this::toDealResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDeal(Long id) {
        Deal deal = dealRepository.findWithProductById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "특가 정보를 찾을 수 없습니다."));
        return toDealResponse(deal);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAdminDeals(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        return dealRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(this::toDealResponse)
                .toList();
    }

    public Map<String, Object> createDeal(AuthenticatedUserPrincipal principal, SaveDealRequest request) {
        requireAdmin(principal);
        Product product = getProduct(request.productId());
        Deal deal = new Deal();
        deal.setProduct(product);
        deal.setTitle(request.title());
        deal.setType(request.type().toUpperCase());
        deal.setDescription(request.description());
        deal.setDealPrice(request.dealPrice());
        deal.setDiscountRate(request.discountRate() == null ? 0 : request.discountRate());
        deal.setStock(request.stock() == null ? 0 : request.stock());
        deal.setBannerUrl(request.bannerUrl());
        deal.setStartAt(request.startAt());
        deal.setEndAt(request.endAt());
        validateDates(deal.getStartAt(), deal.getEndAt());
        return toDealResponse(dealRepository.save(deal));
    }

    public Map<String, Object> updateDeal(AuthenticatedUserPrincipal principal, Long id, UpdateDealRequest request) {
        requireAdmin(principal);
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "특가 정보를 찾을 수 없습니다."));
        if (request.title() != null && !request.title().isBlank()) {
            deal.setTitle(request.title());
        }
        if (request.type() != null && !request.type().isBlank()) {
            deal.setType(request.type().toUpperCase());
        }
        if (request.description() != null) {
            deal.setDescription(request.description());
        }
        if (request.dealPrice() != null) {
            deal.setDealPrice(request.dealPrice());
        }
        if (request.discountRate() != null) {
            deal.setDiscountRate(request.discountRate());
        }
        if (request.stock() != null) {
            deal.setStock(request.stock());
        }
        if (request.bannerUrl() != null) {
            deal.setBannerUrl(request.bannerUrl());
        }
        if (request.status() != null && !request.status().isBlank()) {
            deal.setStatus(request.status().toUpperCase());
        }
        if (request.startAt() != null) {
            deal.setStartAt(request.startAt());
        }
        if (request.endAt() != null) {
            deal.setEndAt(request.endAt());
        }
        validateDates(deal.getStartAt(), deal.getEndAt());
        return toDealResponse(dealRepository.save(deal));
    }

    public Map<String, Object> deleteDeal(AuthenticatedUserPrincipal principal, Long id) {
        requireAdmin(principal);
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "특가 정보를 찾을 수 없습니다."));
        dealRepository.delete(deal);
        return Map.of("message", "특가 정보가 삭제되었습니다.");
    }

    private Map<String, Object> toDealResponse(Deal deal) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", deal.getId());
        response.put("title", deal.getTitle());
        response.put("type", deal.getType());
        response.put("description", deal.getDescription() == null ? "" : deal.getDescription());
        response.put("dealPrice", deal.getDealPrice());
        response.put("discountRate", deal.getDiscountRate());
        response.put("stock", deal.getStock());
        response.put("bannerUrl", deal.getBannerUrl() == null ? "" : deal.getBannerUrl());
        response.put("status", deal.getStatus());
        response.put("startAt", deal.getStartAt().toString());
        response.put("endAt", deal.getEndAt().toString());
        response.put("product", Map.of(
                "id", deal.getProduct().getId(),
                "name", deal.getProduct().getName(),
                "thumbnailUrl", deal.getProduct().getThumbnailUrl() == null ? "" : deal.getProduct().getThumbnailUrl()
        ));
        return response;
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }

    private void validateDates(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (endAt.isBefore(startAt)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "종료일은 시작일보다 빠를 수 없습니다.");
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
