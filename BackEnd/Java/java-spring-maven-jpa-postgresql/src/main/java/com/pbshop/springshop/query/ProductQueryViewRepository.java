package com.pbshop.springshop.query;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductQueryViewRepository extends JpaRepository<ProductQueryView, Long> {
    Optional<ProductQueryView> findByProductId(Long productId);
}
