package com.pbshop.springshop.activity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecentProductViewRepository extends JpaRepository<RecentProductView, Long> {

    @EntityGraph(attributePaths = {"product", "product.category"})
    List<RecentProductView> findByUserIdOrderByViewedAtDesc(Long userId);

    Optional<RecentProductView> findByUserIdAndProductId(Long userId, Long productId);

    long countByUserId(Long userId);
}
