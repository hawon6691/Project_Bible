package com.pbshop.java.spring.maven.jpa.postgresql.fraud;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudFlagRepository extends JpaRepository<FraudFlag, Long> {

    @EntityGraph(attributePaths = {"product", "priceEntry"})
    List<FraudFlag> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = {"product", "priceEntry"})
    List<FraudFlag> findByStatusOrderByIdDesc(String status);

    @EntityGraph(attributePaths = {"product", "priceEntry"})
    List<FraudFlag> findByProductIdOrderByIdDesc(Long productId);
}
