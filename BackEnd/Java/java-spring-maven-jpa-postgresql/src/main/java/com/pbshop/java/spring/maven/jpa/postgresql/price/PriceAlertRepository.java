package com.pbshop.java.spring.maven.jpa.postgresql.price;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {

    @EntityGraph(attributePaths = {"product"})
    List<PriceAlert> findByUserIdOrderByIdDesc(Long userId);

    @EntityGraph(attributePaths = {"product"})
    Optional<PriceAlert> findByUserIdAndProductId(Long userId, Long productId);
}
