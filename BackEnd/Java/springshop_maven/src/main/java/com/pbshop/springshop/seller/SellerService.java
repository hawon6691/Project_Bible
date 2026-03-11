package com.pbshop.springshop.seller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.product.Seller;
import com.pbshop.springshop.product.SellerRepository;
import com.pbshop.springshop.seller.dto.SellerDtos.SaveSellerRequest;

@Service
@Transactional
public class SellerService {

    private final SellerRepository sellerRepository;

    public SellerService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSellers(int page, int limit, String search) {
        String keyword = search == null ? "" : search;
        Page<Seller> result = sellerRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                keyword,
                keyword,
                PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1))
        );

        return Map.of(
                "items", result.getContent().stream().map(this::toSellerResponse).toList(),
                "pagination", Map.of(
                        "page", page,
                        "limit", limit,
                        "total", result.getTotalElements(),
                        "totalPages", result.getTotalPages()
                )
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "판매처를 찾을 수 없습니다."));
        return toSellerResponse(seller);
    }

    public Map<String, Object> createSeller(AuthenticatedUserPrincipal principal, SaveSellerRequest request) {
        requireAdmin(principal);
        if (sellerRepository.existsByCode(request.code())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미 사용 중인 판매처 코드입니다.");
        }
        Seller seller = new Seller();
        applyRequest(seller, request);
        return toSellerResponse(sellerRepository.save(seller));
    }

    public Map<String, Object> updateSeller(
            AuthenticatedUserPrincipal principal,
            Long id,
            SaveSellerRequest request
    ) {
        requireAdmin(principal);
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "판매처를 찾을 수 없습니다."));
        if (sellerRepository.existsByCodeAndIdNot(request.code(), id)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미 사용 중인 판매처 코드입니다.");
        }
        applyRequest(seller, request);
        return toSellerResponse(sellerRepository.save(seller));
    }

    public Map<String, Object> deleteSeller(AuthenticatedUserPrincipal principal, Long id) {
        requireAdmin(principal);
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "판매처를 찾을 수 없습니다."));
        sellerRepository.delete(seller);
        return Map.of("message", "판매처가 삭제되었습니다.");
    }

    private void applyRequest(Seller seller, SaveSellerRequest request) {
        seller.setName(request.name());
        seller.setCode(request.code());
        seller.setHomepageUrl(request.homepageUrl());
        seller.setStatus(request.status() == null || request.status().isBlank() ? "ACTIVE" : request.status());
    }

    private Map<String, Object> toSellerResponse(Seller seller) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", seller.getId());
        response.put("name", seller.getName());
        response.put("code", seller.getCode());
        response.put("homepageUrl", seller.getHomepageUrl() == null ? "" : seller.getHomepageUrl());
        response.put("status", seller.getStatus());
        return response;
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
