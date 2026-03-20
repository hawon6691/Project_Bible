package com.pbshop.java.spring.maven.jpa.postgresql.compare;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompareItemRepository extends JpaRepository<CompareItem, Long> {
    @EntityGraph(attributePaths = {"product", "product.category"})
    List<CompareItem> findByCompareKeyOrderByIdAsc(String compareKey);
    @EntityGraph(attributePaths = {"product", "product.category"})
    Optional<CompareItem> findByCompareKeyAndProductId(String compareKey, Long productId);
    void deleteByCompareKeyAndProductId(String compareKey, Long productId);
}
