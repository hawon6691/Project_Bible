package com.pbshop.springshop.price;

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
import com.pbshop.springshop.price.dto.PriceDtos.CreatePriceAlertRequest;
import com.pbshop.springshop.price.dto.PriceDtos.SavePriceEntryRequest;
import com.pbshop.springshop.price.dto.PriceDtos.UpdatePriceEntryRequest;
import com.pbshop.springshop.product.PriceEntry;
import com.pbshop.springshop.product.PriceEntryRepository;
import com.pbshop.springshop.product.Product;
import com.pbshop.springshop.product.ProductRepository;
import com.pbshop.springshop.product.Seller;
import com.pbshop.springshop.product.SellerRepository;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@Service
@Transactional
public class PriceService {

    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final PriceEntryRepository priceEntryRepository;
    private final PriceAlertRepository priceAlertRepository;
    private final UserRepository userRepository;

    public PriceService(
            ProductRepository productRepository,
            SellerRepository sellerRepository,
            PriceEntryRepository priceEntryRepository,
            PriceAlertRepository priceAlertRepository,
            UserRepository userRepository
    ) {
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
        this.priceEntryRepository = priceEntryRepository;
        this.priceAlertRepository = priceAlertRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProductPrices(Long productId) {
        Product product = ensureProduct(productId);
        List<PriceEntry> entries = priceEntryRepository.findByProductIdOrderByPriceAsc(productId);

        BigDecimal lowest = entries.stream().map(PriceEntry::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal highest = entries.stream().map(PriceEntry::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal average = entries.stream().map(PriceEntry::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!entries.isEmpty()) {
            average = average.divide(BigDecimal.valueOf(entries.size()), 2, RoundingMode.HALF_UP);
        }

        return Map.of(
                "productId", product.getId(),
                "productName", product.getName(),
                "lowestPrice", lowest,
                "highestPrice", highest,
                "averagePrice", average,
                "entries", entries.stream().map(this::toPriceEntryResponse).toList()
        );
    }

    public Map<String, Object> createPriceEntry(
            AuthenticatedUserPrincipal principal,
            Long productId,
            SavePriceEntryRequest request
    ) {
        requireSellerOrAdmin(principal);
        Product product = ensureProduct(productId);
        Seller seller = sellerRepository.findById(request.sellerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "판매처를 찾을 수 없습니다."));

        PriceEntry entry = new PriceEntry();
        entry.setProduct(product);
        entry.setSeller(seller);
        applyPriceRequest(entry, request.price(), request.originalPrice(), request.shippingFee(), request.stockStatus(), request.purchaseUrl());
        return toPriceEntryResponse(priceEntryRepository.save(entry));
    }

    public Map<String, Object> updatePriceEntry(
            AuthenticatedUserPrincipal principal,
            Long id,
            UpdatePriceEntryRequest request
    ) {
        requireSellerOrAdmin(principal);
        PriceEntry entry = priceEntryRepository.findDetailById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "가격 정보를 찾을 수 없습니다."));

        applyPriceRequest(
                entry,
                request.price() == null ? entry.getPrice() : request.price(),
                request.originalPrice(),
                request.shippingFee() == null ? entry.getShippingFee() : request.shippingFee(),
                request.stockStatus() == null || request.stockStatus().isBlank() ? entry.getStockStatus() : request.stockStatus(),
                request.purchaseUrl() == null ? entry.getPurchaseUrl() : request.purchaseUrl()
        );
        return toPriceEntryResponse(priceEntryRepository.save(entry));
    }

    public Map<String, Object> deletePriceEntry(AuthenticatedUserPrincipal principal, Long id) {
        requireAdmin(principal);
        PriceEntry entry = priceEntryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "가격 정보를 찾을 수 없습니다."));
        priceEntryRepository.delete(entry);
        return Map.of("message", "가격 정보가 삭제되었습니다.");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPriceHistory(Long productId, Integer days) {
        ensureProduct(productId);
        int window = days == null || days <= 0 ? 30 : days;
        OffsetDateTime from = OffsetDateTime.now().minusDays(window);

        List<Map<String, Object>> items = priceEntryRepository.findHistoryByProductIdSince(productId, from).stream()
                .map(entry -> {
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("id", entry.getId());
                    response.put("sellerId", entry.getSeller().getId());
                    response.put("sellerName", entry.getSeller().getName());
                    response.put("price", entry.getPrice());
                    response.put("checkedAt", entry.getCheckedAt() == null ? null : entry.getCheckedAt().toString());
                    return response;
                })
                .toList();

        return Map.of(
                "productId", productId,
                "days", window,
                "items", items
        );
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAlerts(AuthenticatedUserPrincipal principal) {
        User user = requireUser(principal);
        return priceAlertRepository.findByUserIdOrderByIdDesc(user.getId()).stream()
                .map(this::toPriceAlertResponse)
                .toList();
    }

    public Map<String, Object> createAlert(
            AuthenticatedUserPrincipal principal,
            CreatePriceAlertRequest request
    ) {
        User user = requireUser(principal);
        Product product = ensureProduct(request.productId());
        PriceAlert alert = priceAlertRepository.findByUserIdAndProductId(user.getId(), product.getId())
                .orElseGet(PriceAlert::new);
        alert.setUser(user);
        alert.setProduct(product);
        alert.setTargetPrice(request.targetPrice());
        alert.setActive(true);
        return toPriceAlertResponse(priceAlertRepository.save(alert));
    }

    public Map<String, Object> deleteAlert(AuthenticatedUserPrincipal principal, Long id) {
        User user = requireUser(principal);
        PriceAlert alert = priceAlertRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "가격 알림을 찾을 수 없습니다."));
        if (!alert.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 가격 알림만 삭제할 수 있습니다.");
        }
        priceAlertRepository.delete(alert);
        return Map.of("message", "가격 알림이 삭제되었습니다.");
    }

    private void applyPriceRequest(
            PriceEntry entry,
            BigDecimal price,
            BigDecimal originalPrice,
            BigDecimal shippingFee,
            String stockStatus,
            String purchaseUrl
    ) {
        entry.setPrice(price);
        entry.setOriginalPrice(originalPrice);
        entry.setShippingFee(shippingFee == null ? BigDecimal.ZERO : shippingFee);
        entry.setStockStatus(stockStatus == null || stockStatus.isBlank() ? "IN_STOCK" : stockStatus);
        entry.setPurchaseUrl(purchaseUrl);
    }

    private Map<String, Object> toPriceEntryResponse(PriceEntry entry) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", entry.getId());
        response.put("productId", entry.getProduct().getId());
        response.put("sellerId", entry.getSeller().getId());
        response.put("sellerName", entry.getSeller().getName());
        response.put("price", entry.getPrice());
        response.put("originalPrice", entry.getOriginalPrice());
        response.put("shippingFee", entry.getShippingFee());
        response.put("stockStatus", entry.getStockStatus());
        response.put("purchaseUrl", entry.getPurchaseUrl() == null ? "" : entry.getPurchaseUrl());
        return response;
    }

    private Map<String, Object> toPriceAlertResponse(PriceAlert alert) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", alert.getId());
        response.put("productId", alert.getProduct().getId());
        response.put("productName", alert.getProduct().getName());
        response.put("targetPrice", alert.getTargetPrice());
        response.put("active", alert.isActive());
        return response;
    }

    private Product ensureProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }

    private User requireUser(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return userRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "인증 사용자를 찾을 수 없습니다."));
    }

    private void requireSellerOrAdmin(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        String role = principal.role();
        if (!"ADMIN".equalsIgnoreCase(role) && !"SELLER".equalsIgnoreCase(role)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "판매자 또는 관리자 권한이 필요합니다.");
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
