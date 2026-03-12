package com.pbshop.springshop.matching;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMappingRepository extends JpaRepository<ProductMapping, Long> {
    @EntityGraph(attributePaths = {"product"})
    List<ProductMapping> findByStatusOrderByIdDesc(String status);
    long countByStatus(String status);
}
