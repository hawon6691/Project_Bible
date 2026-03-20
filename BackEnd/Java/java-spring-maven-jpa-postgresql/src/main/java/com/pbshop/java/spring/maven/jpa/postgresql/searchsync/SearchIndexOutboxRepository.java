package com.pbshop.java.spring.maven.jpa.postgresql.searchsync;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchIndexOutboxRepository extends JpaRepository<SearchIndexOutbox, Long> {
    long countByStatus(String status);
    List<SearchIndexOutbox> findByStatusOrderByIdAsc(String status, Pageable pageable);
}
