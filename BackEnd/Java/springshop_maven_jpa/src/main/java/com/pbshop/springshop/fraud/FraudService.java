package com.pbshop.springshop.fraud;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.product.PriceEntry;
import com.pbshop.springshop.product.PriceEntryRepository;
import com.pbshop.springshop.product.Product;
import com.pbshop.springshop.product.ProductRepository;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@Service
@Transactional
public class FraudService {

    private static final String ANOMALY_REASON = "평균 가격 대비 급격히 낮은 가격이 감지되었습니다.";

    private final FraudFlagRepository fraudFlagRepository;
    private final ProductRepository productRepository;
    private final PriceEntryRepository priceEntryRepository;
    private final UserRepository userRepository;

    public FraudService(
            FraudFlagRepository fraudFlagRepository,
            ProductRepository productRepository,
            PriceEntryRepository priceEntryRepository,
            UserRepository userRepository
    ) {
        this.fraudFlagRepository = fraudFlagRepository;
        this.productRepository = productRepository;
        this.priceEntryRepository = priceEntryRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAlerts(AuthenticatedUserPrincipal principal, String status) {
        requireAdmin(principal);
        List<FraudFlag> flags = status == null || status.isBlank()
                ? fraudFlagRepository.findAllByOrderByIdDesc()
                : fraudFlagRepository.findByStatusOrderByIdDesc(status.trim().toUpperCase());
        return flags.stream().map(this::toFlagResponse).toList();
    }

    public Map<String, Object> approveAlert(AuthenticatedUserPrincipal principal, Long flagId) {
        User actor = requireAdminUser(principal);
        FraudFlag flag = findFlag(flagId);
        flag.setStatus("APPROVED");
        flag.setApprovedBy(actor);
        flag.setApprovedAt(OffsetDateTime.now());
        flag.setRejectedBy(null);
        flag.setRejectedAt(null);
        fraudFlagRepository.save(flag);
        return Map.of("message", "이상 가격 알림이 승인되었습니다.");
    }

    public Map<String, Object> rejectAlert(AuthenticatedUserPrincipal principal, Long flagId) {
        User actor = requireAdminUser(principal);
        FraudFlag flag = findFlag(flagId);
        flag.setStatus("REJECTED");
        flag.setRejectedBy(actor);
        flag.setRejectedAt(OffsetDateTime.now());
        flag.setApprovedBy(null);
        flag.setApprovedAt(null);
        fraudFlagRepository.save(flag);
        return Map.of("message", "이상 가격 알림이 거절되었습니다.");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRealPrice(Long productId, Long sellerId) {
        List<PriceEntry> entries = priceEntryRepository.findByProductIdOrderByCheckedAtDesc(productId).stream()
                .filter(entry -> sellerId == null || entry.getSeller().getId().equals(sellerId))
                .toList();
        PriceEntry entry = entries.stream()
                .min((left, right) -> totalPrice(left).compareTo(totalPrice(right)))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "가격 정보를 찾을 수 없습니다."));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("productPrice", scale(entry.getPrice()));
        response.put("shippingFee", scale(entry.getShippingFee()));
        response.put("totalPrice", scale(totalPrice(entry)));
        response.put("shippingType", entry.getShippingFee().compareTo(BigDecimal.ZERO) > 0 ? "PAID" : "FREE");
        return response;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getEffectivePrices(Long productId) {
        requireProduct(productId);
        return priceEntryRepository.findByProductIdOrderByCheckedAtDesc(productId).stream()
                .map(entry -> {
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("sellerId", entry.getSeller().getId());
                    response.put("sellerName", entry.getSeller().getName());
                    response.put("price", scale(entry.getPrice()));
                    response.put("shippingFee", scale(entry.getShippingFee()));
                    response.put("totalPrice", scale(totalPrice(entry)));
                    return response;
                })
                .toList();
    }

    public Map<String, Object> scan(AuthenticatedUserPrincipal principal, Long productId) {
        requireAdmin(principal);
        return detectAnomalies(productId, true);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detectAnomalies(Long productId, boolean persist) {
        Product product = requireProduct(productId);
        List<PriceEntry> entries = priceEntryRepository.findByProductIdOrderByCheckedAtDesc(product.getId());
        if (entries.isEmpty()) {
            return Map.of("items", List.of());
        }

        BigDecimal average = entries.stream()
                .map(PriceEntry::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(entries.size()), 2, RoundingMode.HALF_UP);
        BigDecimal threshold = average.multiply(new BigDecimal("0.7"));

        List<Map<String, Object>> items = entries.stream()
                .filter(entry -> average.compareTo(BigDecimal.ZERO) > 0 && entry.getPrice().compareTo(threshold) < 0)
                .map(entry -> {
                    if (persist) {
                        FraudFlag flag = new FraudFlag();
                        flag.setProduct(product);
                        flag.setPriceEntry(entry);
                        flag.setStatus("PENDING");
                        flag.setReason(ANOMALY_REASON);
                        flag.setDetectedPrice(scale(entry.getPrice()));
                        flag.setBaselinePrice(scale(average));
                        fraudFlagRepository.save(flag);
                    }

                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("priceEntryId", entry.getId());
                    response.put("detectedPrice", scale(entry.getPrice()));
                    response.put("baselinePrice", scale(average));
                    response.put("reason", ANOMALY_REASON);
                    return response;
                })
                .toList();

        return Map.of("items", items);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getFlags(AuthenticatedUserPrincipal principal, Long productId) {
        requireAdmin(principal);
        requireProduct(productId);
        return fraudFlagRepository.findByProductIdOrderByIdDesc(productId).stream()
                .map(this::toFlagResponse)
                .toList();
    }

    private FraudFlag findFlag(Long flagId) {
        return fraudFlagRepository.findById(flagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "이상 가격 알림을 찾을 수 없습니다."));
    }

    private Product requireProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }

    private void requireAdmin(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }

    private User requireAdminUser(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        return userRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private Map<String, Object> toFlagResponse(FraudFlag flag) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", flag.getId());
        response.put("productId", flag.getProduct().getId());
        response.put("priceEntryId", flag.getPriceEntry() == null ? null : flag.getPriceEntry().getId());
        response.put("status", flag.getStatus());
        response.put("reason", flag.getReason());
        response.put("detectedPrice", scale(flag.getDetectedPrice()));
        response.put("baselinePrice", scale(flag.getBaselinePrice()));
        response.put("createdAt", flag.getCreatedAt() == null ? null : flag.getCreatedAt().toString());
        response.put("updatedAt", flag.getUpdatedAt() == null ? null : flag.getUpdatedAt().toString());
        return response;
    }

    private BigDecimal totalPrice(PriceEntry entry) {
        return entry.getPrice().add(entry.getShippingFee() == null ? BigDecimal.ZERO : entry.getShippingFee());
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }
}
