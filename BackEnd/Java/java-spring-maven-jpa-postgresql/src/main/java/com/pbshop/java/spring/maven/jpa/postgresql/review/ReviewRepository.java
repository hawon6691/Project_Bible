package com.pbshop.java.spring.maven.jpa.postgresql.review;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<Review> findByProductIdOrderByIdDesc(Long productId);

    @EntityGraph(attributePaths = {"user", "product", "order"})
    Optional<Review> findDetailById(Long id);

    boolean existsByUserIdAndProductIdAndOrderId(Long userId, Long productId, Long orderId);

    long countByProductId(Long productId);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.product.id = :productId")
    BigDecimal findAverageRatingByProductId(Long productId);
}
