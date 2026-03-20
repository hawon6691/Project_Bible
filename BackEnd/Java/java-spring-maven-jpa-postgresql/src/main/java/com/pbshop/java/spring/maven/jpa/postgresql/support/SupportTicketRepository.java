package com.pbshop.java.spring.maven.jpa.postgresql.support;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    Page<SupportTicket> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    Page<SupportTicket> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Optional<SupportTicket> findById(Long id);
}
