package com.pbshop.java.spring.maven.jpa.postgresql.order;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"address", "items", "items.product", "items.seller"})
    Optional<Order> findDetailById(Long id);

    @EntityGraph(attributePaths = {"address", "items", "items.product", "items.seller"})
    Optional<Order> findDetailByIdAndUserId(Long id, Long userId);

    Page<Order> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    Page<Order> findAllByOrderByIdDesc(Pageable pageable);
}
