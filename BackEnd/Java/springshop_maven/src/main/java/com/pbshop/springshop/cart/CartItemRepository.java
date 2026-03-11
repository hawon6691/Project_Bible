package com.pbshop.springshop.cart;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @EntityGraph(attributePaths = {"product", "product.priceEntries", "product.priceEntries.seller", "seller"})
    List<CartItem> findByUserIdOrderByIdDesc(Long userId);

    @EntityGraph(attributePaths = {"product", "product.priceEntries", "product.priceEntries.seller", "seller"})
    Optional<CartItem> findByIdAndUserId(Long id, Long userId);

    Optional<CartItem> findByUserIdAndProductIdAndSellerIdAndSelectedOptionsJson(
            Long userId,
            Long productId,
            Long sellerId,
            String selectedOptionsJson
    );

    void deleteByUserId(Long userId);
}
