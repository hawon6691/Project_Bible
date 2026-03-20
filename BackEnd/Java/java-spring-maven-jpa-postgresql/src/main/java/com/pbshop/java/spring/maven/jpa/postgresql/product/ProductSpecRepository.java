package com.pbshop.java.spring.maven.jpa.postgresql.product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSpecRepository extends JpaRepository<ProductSpec, Long> {

    List<ProductSpec> findByProductIdOrderBySortOrderAsc(Long productId);
}
