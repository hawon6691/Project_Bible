package com.pbshop.java.spring.maven.jpa.postgresql.usedmarket;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsedMarketPriceRepository extends JpaRepository<UsedMarketPrice, Long> {
    @EntityGraph(attributePaths = {"product", "product.category"})
    List<UsedMarketPrice> findByProductIdOrderByIdAsc(Long productId);
    @EntityGraph(attributePaths = {"product", "product.category"})
    List<UsedMarketPrice> findByProductCategoryIdOrderByProductIdAscIdAsc(Long categoryId);
}
