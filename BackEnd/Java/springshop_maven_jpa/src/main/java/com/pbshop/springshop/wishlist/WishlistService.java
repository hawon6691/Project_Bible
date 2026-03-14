package com.pbshop.springshop.wishlist;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.product.Product;
import com.pbshop.springshop.product.ProductRepository;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@Service
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public WishlistService(WishlistRepository wishlistRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getWishlist(AuthenticatedUserPrincipal principal, int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), limit, Sort.by(Sort.Direction.DESC, "id"));
        Page<WishlistItem> wishlistPage = wishlistRepository.findByUserIdOrderByIdDesc(principal.userId(), pageable);

        return Map.of(
                "items", wishlistPage.getContent().stream().map(this::toResponse).toList(),
                "page", wishlistPage.getNumber() + 1,
                "limit", wishlistPage.getSize(),
                "total", wishlistPage.getTotalElements(),
                "totalPages", wishlistPage.getTotalPages()
        );
    }

    @Transactional
    public Map<String, Object> toggle(AuthenticatedUserPrincipal principal, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "상품을 찾을 수 없습니다."));

        return wishlistRepository.findByUserIdAndProductId(principal.userId(), productId)
                .map(existing -> {
                    wishlistRepository.delete(existing);
                    return Map.<String, Object>of("wishlisted", false, "productId", productId);
                })
                .orElseGet(() -> {
                    User user = userRepository.findById(principal.userId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
                    WishlistItem item = new WishlistItem();
                    item.setUser(user);
                    item.setProduct(product);
                    wishlistRepository.save(item);
                    return Map.<String, Object>of("wishlisted", true, "productId", productId);
                });
    }

    @Transactional
    public Map<String, Object> remove(AuthenticatedUserPrincipal principal, Long productId) {
        WishlistItem item = wishlistRepository.findByUserIdAndProductId(principal.userId(), productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "위시리스트 항목을 찾을 수 없습니다."));
        wishlistRepository.delete(item);
        return Map.of("message", "위시리스트에서 제거되었습니다.");
    }

    private Map<String, Object> toResponse(WishlistItem item) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", item.getId());
        response.put("productId", item.getProduct().getId());
        response.put("productName", item.getProduct().getName());
        response.put("thumbnailUrl", item.getProduct().getThumbnailUrl());
        response.put("categoryId", item.getProduct().getCategory() == null ? null : item.getProduct().getCategory().getId());
        response.put("createdAt", item.getCreatedAt());
        return response;
    }
}
