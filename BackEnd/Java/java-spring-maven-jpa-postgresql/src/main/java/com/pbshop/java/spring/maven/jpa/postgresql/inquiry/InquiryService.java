package com.pbshop.java.spring.maven.jpa.postgresql.inquiry;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.inquiry.dto.InquiryDtos.AnswerInquiryRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.inquiry.dto.InquiryDtos.CreateInquiryRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.product.Product;
import com.pbshop.java.spring.maven.jpa.postgresql.product.ProductRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.user.User;
import com.pbshop.java.spring.maven.jpa.postgresql.user.UserRepository;

@Service
@Transactional(readOnly = true)
public class InquiryService {

    private final ProductInquiryRepository inquiryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public InquiryService(
            ProductInquiryRepository inquiryRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.inquiryRepository = inquiryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Map<String, Object>> getProductInquiries(Long productId) {
        Product product = getProduct(productId);
        return inquiryRepository.findByProductIdOrderByIdDesc(product.getId()).stream()
                .map(inquiry -> toResponse(inquiry, false))
                .toList();
    }

    @Transactional
    public Map<String, Object> createInquiry(AuthenticatedUserPrincipal principal, Long productId, CreateInquiryRequest request) {
        ProductInquiry inquiry = new ProductInquiry();
        inquiry.setProduct(getProduct(productId));
        inquiry.setUser(getUser(principal.userId()));
        inquiry.setTitle(request.title());
        inquiry.setContent(request.content());
        inquiry.setSecret(request.isSecret());
        inquiry.setStatus("PENDING");
        return toResponse(inquiryRepository.save(inquiry), true);
    }

    @Transactional
    public Map<String, Object> answerInquiry(AuthenticatedUserPrincipal principal, Long inquiryId, AnswerInquiryRequest request) {
        requireSellerOrAdmin(principal);
        ProductInquiry inquiry = getInquiry(inquiryId);
        inquiry.setAnswerContent(request.answer());
        inquiry.setStatus("ANSWERED");
        inquiry.setAnsweredBy(getUser(principal.userId()));
        inquiry.setAnsweredAt(OffsetDateTime.now());
        return toResponse(inquiryRepository.save(inquiry), true);
    }

    public List<Map<String, Object>> getMyInquiries(AuthenticatedUserPrincipal principal) {
        return inquiryRepository.findByUserIdOrderByIdDesc(principal.userId()).stream()
                .map(inquiry -> toResponse(inquiry, true))
                .toList();
    }

    @Transactional
    public Map<String, Object> deleteInquiry(AuthenticatedUserPrincipal principal, Long inquiryId) {
        ProductInquiry inquiry = getInquiry(inquiryId);
        if (!principal.userId().equals(inquiry.getUser().getId()) && !"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        inquiryRepository.delete(inquiry);
        return Map.of("message", "문의가 삭제되었습니다.");
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private ProductInquiry getInquiry(Long inquiryId) {
        return inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "문의가 존재하지 않습니다."));
    }

    private void requireSellerOrAdmin(AuthenticatedUserPrincipal principal) {
        if (!"SELLER".equalsIgnoreCase(principal.role()) && !"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private Map<String, Object> toResponse(ProductInquiry inquiry, boolean revealSecret) {
        boolean visibleSecret = revealSecret || !inquiry.isSecret();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", inquiry.getId());
        response.put("productId", inquiry.getProduct().getId());
        response.put("userId", inquiry.getUser().getId());
        response.put("userName", inquiry.getUser().getName());
        response.put("title", inquiry.getTitle());
        response.put("content", visibleSecret ? inquiry.getContent() : "비밀 문의입니다.");
        response.put("isSecret", inquiry.isSecret());
        response.put("status", inquiry.getStatus());
        response.put("answer", inquiry.getAnswerContent());
        response.put("answeredByUserId", inquiry.getAnsweredBy() == null ? null : inquiry.getAnsweredBy().getId());
        response.put("answeredAt", inquiry.getAnsweredAt());
        response.put("createdAt", inquiry.getCreatedAt());
        return response;
    }
}
