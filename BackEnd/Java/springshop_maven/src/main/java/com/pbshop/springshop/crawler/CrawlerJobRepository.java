package com.pbshop.springshop.crawler;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawlerJobRepository extends JpaRepository<CrawlerJob, Long> {
    Page<CrawlerJob> findByStatusOrderByIdDesc(String status, Pageable pageable);
    Page<CrawlerJob> findAllByOrderByIdDesc(Pageable pageable);
}
