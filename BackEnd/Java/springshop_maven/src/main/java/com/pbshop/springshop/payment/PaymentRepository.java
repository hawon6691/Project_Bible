package com.pbshop.springshop.payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {"order", "order.items", "order.items.product", "order.items.seller"})
    Optional<Payment> findDetailById(Long id);

    @EntityGraph(attributePaths = {"order", "order.items", "order.items.product", "order.items.seller"})
    Optional<Payment> findDetailByIdAndUserId(Long id, Long userId);
}
