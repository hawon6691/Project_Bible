package com.pbshop.java.spring.maven.jpa.postgresql.compare;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.product.PriceEntryRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.product.Product;
import com.pbshop.java.spring.maven.jpa.postgresql.product.ProductRepository;

@Service
@Transactional
public class CompareService {

    private final CompareItemRepository compareItemRepository;
    private final ProductRepository productRepository;
    private final PriceEntryRepository priceEntryRepository;

    public CompareService(
            CompareItemRepository compareItemRepository,
            ProductRepository productRepository,
            PriceEntryRepository priceEntryRepository
    ) {
        this.compareItemRepository = compareItemRepository;
        this.productRepository = productRepository;
        this.priceEntryRepository = priceEntryRepository;
    }

    public Map<String, Object> add(String compareKey, Long productId) {
        Product product = requireProduct(productId);
        compareItemRepository.findByCompareKeyAndProductId(compareKey, productId)
                .orElseGet(() -> {
                    CompareItem item = new CompareItem();
                    item.setCompareKey(compareKey);
                    item.setProduct(product);
                    return compareItemRepository.save(item);
                });
        return list(compareKey);
    }

    public Map<String, Object> remove(String compareKey, Long productId) {
        compareItemRepository.deleteByCompareKeyAndProductId(compareKey, productId);
        return Map.of("message", "비교 상품이 삭제되었습니다.");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> list(String compareKey) {
        List<Map<String, Object>> compareList = compareItemRepository.findByCompareKeyOrderByIdAsc(compareKey).stream()
                .map(item -> {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("productId", item.getProduct().getId());
                    data.put("name", item.getProduct().getName());
                    data.put("slug", item.getProduct().getSlug());
                    data.put("thumbnailUrl", item.getProduct().getThumbnailUrl() == null ? "" : item.getProduct().getThumbnailUrl());
                    return data;
                })
                .toList();
        return Map.of("compareKey", compareKey, "compareList", compareList);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> detail(String compareKey) {
        List<Map<String, Object>> items = compareItemRepository.findByCompareKeyOrderByIdAsc(compareKey).stream()
                .map(item -> {
                    Product product = item.getProduct();
                    BigDecimal bestPrice = priceEntryRepository.findByProductIdOrderByPriceAsc(product.getId()).stream()
                            .map(entry -> entry.getPrice().add(entry.getShippingFee()))
                            .min(BigDecimal::compareTo)
                            .orElse(BigDecimal.ZERO)
                            .setScale(2, RoundingMode.HALF_UP);
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("productId", product.getId());
                    data.put("name", product.getName());
                    data.put("slug", product.getSlug());
                    data.put("categoryId", product.getCategory().getId());
                    data.put("bestPrice", bestPrice);
                    data.put("ratingAvg", product.getRatingAvg());
                    return data;
                })
                .toList();
        return Map.of("compareKey", compareKey, "items", items);
    }

    private Product requireProduct(Long productId) {
        return productRepository.findDetailById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }
}
