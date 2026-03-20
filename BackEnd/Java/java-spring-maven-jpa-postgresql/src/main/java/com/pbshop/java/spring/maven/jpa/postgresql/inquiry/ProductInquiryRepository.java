package com.pbshop.java.spring.maven.jpa.postgresql.inquiry;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductInquiryRepository extends JpaRepository<ProductInquiry, Long> {

    @EntityGraph(attributePaths = {"user", "answeredBy"})
    List<ProductInquiry> findByProductIdOrderByIdDesc(Long productId);

    @EntityGraph(attributePaths = {"product", "user", "answeredBy"})
    List<ProductInquiry> findByUserIdOrderByIdDesc(Long userId);

    @EntityGraph(attributePaths = {"product", "user", "answeredBy"})
    Optional<ProductInquiry> findById(Long id);
}
