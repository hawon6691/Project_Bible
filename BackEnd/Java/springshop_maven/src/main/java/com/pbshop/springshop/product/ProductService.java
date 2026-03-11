package com.pbshop.springshop.product;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.category.Category;
import com.pbshop.springshop.category.CategoryRepository;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.product.dto.ProductDtos.SaveProductRequest;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSpecRepository productSpecRepository;
    private final PriceEntryRepository priceEntryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductSpecRepository productSpecRepository,
            PriceEntryRepository priceEntryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productSpecRepository = productSpecRepository;
        this.priceEntryRepository = priceEntryRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProducts(
            Long categoryId,
            String search,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort,
            int page,
            int limit
    ) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1), toSort(sort));
        Page<Product> result = productRepository.search(categoryId, blankToNull(search), minPrice, maxPrice, pageable);

        return Map.of(
                "items", result.getContent().stream().map(this::toProductListItem).toList(),
                "pagination", Map.of(
                        "page", page,
                        "limit", limit,
                        "total", result.getTotalElements(),
                        "totalPages", result.getTotalPages()
                )
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProduct(Long id) {
        Product product = productRepository.findDetailById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));

        Map<String, Object> response = new LinkedHashMap<>(toProductListItem(product));
        response.put("category", Map.of(
                "id", product.getCategory().getId(),
                "name", product.getCategory().getName(),
                "slug", product.getCategory().getSlug()
        ));
        response.put("specs", productSpecRepository.findByProductIdOrderBySortOrderAsc(product.getId()).stream()
                .map(spec -> Map.of(
                        "id", spec.getId(),
                        "key", spec.getSpecKey(),
                        "value", spec.getSpecValue(),
                        "sortOrder", spec.getSortOrder()
                ))
                .toList());
        response.put("priceEntries", priceEntryRepository.findByProductIdOrderByPriceAsc(product.getId()).stream()
                .map(priceEntry -> Map.of(
                        "id", priceEntry.getId(),
                        "sellerId", priceEntry.getSeller().getId(),
                        "sellerName", priceEntry.getSeller().getName(),
                        "price", priceEntry.getPrice(),
                        "originalPrice", priceEntry.getOriginalPrice(),
                        "shippingFee", priceEntry.getShippingFee(),
                        "stockStatus", priceEntry.getStockStatus(),
                        "purchaseUrl", priceEntry.getPurchaseUrl()
                ))
                .toList());
        return response;
    }

    public Map<String, Object> createProduct(AuthenticatedUserPrincipal principal, SaveProductRequest request) {
        requireAdmin(principal);
        Product product = new Product();
        applyRequest(product, request, null);
        return toProductListItem(productRepository.save(product));
    }

    public Map<String, Object> updateProduct(AuthenticatedUserPrincipal principal, Long id, SaveProductRequest request) {
        requireAdmin(principal);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
        applyRequest(product, request, id);
        return toProductListItem(productRepository.save(product));
    }

    public Map<String, Object> deleteProduct(AuthenticatedUserPrincipal principal, Long id) {
        requireAdmin(principal);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));
        productRepository.delete(product);
        return Map.of("message", "상품이 삭제되었습니다.");
    }

    private void applyRequest(Product product, SaveProductRequest request, Long currentId) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다."));

        String slug = request.slug() == null || request.slug().isBlank() ? slugify(request.name()) : slugify(request.slug());
        boolean duplicate = currentId == null ? productRepository.existsBySlug(slug) : productRepository.existsBySlugAndIdNot(slug, currentId);
        if (duplicate) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "이미 사용 중인 상품 slug 입니다.");
        }

        product.setCategory(category);
        product.setName(request.name());
        product.setSlug(slug);
        product.setBrand(request.brand());
        product.setDescription(request.description());
        product.setThumbnailUrl(request.thumbnailUrl());
        product.setReviewCount(request.reviewCount() == null ? 0 : request.reviewCount());
        product.setRatingAvg(request.ratingAvg() == null ? BigDecimal.ZERO : request.ratingAvg());
        product.setStatus(request.status() == null || request.status().isBlank() ? "ACTIVE" : request.status());
    }

    private void requireAdmin(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }

    private Map<String, Object> toProductListItem(Product product) {
        BigDecimal lowestPrice = product.getPriceEntries().stream()
                .map(PriceEntry::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal averagePrice = product.getPriceEntries().stream()
                .map(PriceEntry::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int sellerCount = product.getPriceEntries().size();
        if (sellerCount > 0) {
            averagePrice = averagePrice.divide(BigDecimal.valueOf(sellerCount), 2, java.math.RoundingMode.HALF_UP);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", product.getId());
        response.put("categoryId", product.getCategory().getId());
        response.put("name", product.getName());
        response.put("slug", product.getSlug());
        response.put("brand", product.getBrand() == null ? "" : product.getBrand());
        response.put("description", product.getDescription() == null ? "" : product.getDescription());
        response.put("thumbnailUrl", product.getThumbnailUrl() == null ? "" : product.getThumbnailUrl());
        response.put("reviewCount", product.getReviewCount());
        response.put("ratingAvg", product.getRatingAvg());
        response.put("status", product.getStatus());
        response.put("lowestPrice", lowestPrice);
        response.put("averagePrice", averagePrice);
        response.put("sellerCount", sellerCount);
        return response;
    }

    private Sort toSort(String sort) {
        if ("popularity".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "reviewCount").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        if ("rating_desc".equalsIgnoreCase(sort) || "rating".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "ratingAvg").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        return Sort.by(Sort.Direction.DESC, "id");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        return normalized.isBlank() ? "product" : normalized;
    }
}
