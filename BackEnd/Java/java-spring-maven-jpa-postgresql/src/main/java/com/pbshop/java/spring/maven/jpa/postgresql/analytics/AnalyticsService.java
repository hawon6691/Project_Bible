package com.pbshop.java.spring.maven.jpa.postgresql.analytics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.product.PriceEntry;
import com.pbshop.java.spring.maven.jpa.postgresql.product.PriceEntryRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.product.Product;
import com.pbshop.java.spring.maven.jpa.postgresql.product.ProductRepository;

@Service
@Transactional
public class AnalyticsService {

    private final ProductRepository productRepository;
    private final PriceEntryRepository priceEntryRepository;

    public AnalyticsService(ProductRepository productRepository, PriceEntryRepository priceEntryRepository) {
        this.productRepository = productRepository;
        this.priceEntryRepository = priceEntryRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> lowestEver(Long productId) {
        Product product = requireProduct(productId);
        PriceEntry lowest = priceEntryRepository.findByProductIdOrderByPriceAsc(productId).stream()
                .min(Comparator.comparing(PriceEntry::getPrice))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "가격 이력을 찾을 수 없습니다."));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("productId", product.getId());
        response.put("productName", product.getName());
        response.put("lowestPrice", lowest.getPrice().setScale(2, RoundingMode.HALF_UP));
        response.put("sellerId", lowest.getSeller().getId());
        response.put("checkedAt", lowest.getCheckedAt() == null ? null : lowest.getCheckedAt().toString());
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> unitPrice(Long productId) {
        Product product = requireProduct(productId);
        BigDecimal price = priceEntryRepository.findByProductIdOrderByPriceAsc(productId).stream()
                .map(PriceEntry::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("productId", product.getId());
        response.put("productName", product.getName());
        response.put("unit", "ea");
        response.put("quantity", 1);
        response.put("unitPrice", price);
        return response;
    }

    private Product requireProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }
}
