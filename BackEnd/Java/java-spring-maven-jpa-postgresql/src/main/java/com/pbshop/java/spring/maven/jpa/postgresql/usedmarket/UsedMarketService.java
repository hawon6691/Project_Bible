package com.pbshop.java.spring.maven.jpa.postgresql.usedmarket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.pcbuilder.PcBuild;
import com.pbshop.java.spring.maven.jpa.postgresql.pcbuilder.PcBuildPart;
import com.pbshop.java.spring.maven.jpa.postgresql.pcbuilder.PcBuildPartRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.pcbuilder.PcBuildRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.product.Product;
import com.pbshop.java.spring.maven.jpa.postgresql.product.ProductRepository;

@Service
@Transactional
public class UsedMarketService {

    private final UsedMarketPriceRepository usedMarketPriceRepository;
    private final ProductRepository productRepository;
    private final PcBuildRepository pcBuildRepository;
    private final PcBuildPartRepository pcBuildPartRepository;

    public UsedMarketService(
            UsedMarketPriceRepository usedMarketPriceRepository,
            ProductRepository productRepository,
            PcBuildRepository pcBuildRepository,
            PcBuildPartRepository pcBuildPartRepository
    ) {
        this.usedMarketPriceRepository = usedMarketPriceRepository;
        this.productRepository = productRepository;
        this.pcBuildRepository = pcBuildRepository;
        this.pcBuildPartRepository = pcBuildPartRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> productPrice(Long productId) {
        Product product = requireProduct(productId);
        List<UsedMarketPrice> prices = usedMarketPriceRepository.findByProductIdOrderByIdAsc(productId);
        return summarizeProduct(product, prices);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> categoryPrices(Long categoryId, int page, int limit) {
        List<Map<String, Object>> items = usedMarketPriceRepository.findByProductCategoryIdOrderByProductIdAscIdAsc(categoryId).stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()))
                .values().stream()
                .map(prices -> summarizeProduct(prices.get(0).getProduct(), prices))
                .toList();
        return Map.of(
                "items", items,
                "pagination", Map.of("page", page, "limit", limit, "total", items.size(), "totalPages", items.isEmpty() ? 0 : 1)
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> estimateBuild(AuthenticatedUserPrincipal principal, Long buildId) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        PcBuild build = pcBuildRepository.findByIdAndUserId(buildId, principal.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "PC 빌드를 찾을 수 없습니다."));
        List<Map<String, Object>> partBreakdown = pcBuildPartRepository.findByPcBuildIdOrderByIdAsc(build.getId()).stream()
                .map(this::toPartBreakdown)
                .toList();
        BigDecimal estimatedPrice = partBreakdown.stream()
                .map(item -> (BigDecimal) item.get("estimatedPrice"))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        return Map.of(
                "buildId", build.getId(),
                "estimatedPrice", estimatedPrice,
                "partBreakdown", partBreakdown
        );
    }

    private Map<String, Object> summarizeProduct(Product product, List<UsedMarketPrice> prices) {
        if (prices.isEmpty()) {
            return Map.of(
                    "productId", product.getId(),
                    "productName", product.getName(),
                    "averagePrice", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    "minPrice", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    "maxPrice", BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    "sampleCount", 0
            );
        }
        BigDecimal sum = prices.stream().map(UsedMarketPrice::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = sum.divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);
        BigDecimal min = prices.stream().map(UsedMarketPrice::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal max = prices.stream().map(UsedMarketPrice::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        return Map.of(
                "productId", product.getId(),
                "productName", product.getName(),
                "averagePrice", average,
                "minPrice", min.setScale(2, RoundingMode.HALF_UP),
                "maxPrice", max.setScale(2, RoundingMode.HALF_UP),
                "sampleCount", prices.size()
        );
    }

    private Map<String, Object> toPartBreakdown(PcBuildPart part) {
        List<UsedMarketPrice> prices = usedMarketPriceRepository.findByProductIdOrderByIdAsc(part.getProduct().getId());
        BigDecimal estimated = prices.stream()
                .map(UsedMarketPrice::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO)
                .multiply(BigDecimal.valueOf(part.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        return Map.of(
                "partType", part.getPartType(),
                "productId", part.getProduct().getId(),
                "productName", part.getProduct().getName(),
                "quantity", part.getQuantity(),
                "estimatedPrice", estimated
        );
    }

    private Product requireProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
    }
}
