package com.pbshop.springshop.crawler;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawlerRunRepository extends JpaRepository<CrawlerRun, Long> {
    Page<CrawlerRun> findByStatusOrderByIdDesc(String status, Pageable pageable);
    Page<CrawlerRun> findByCrawlerJobIdOrderByIdDesc(Long crawlerJobId, Pageable pageable);
    Page<CrawlerRun> findByCrawlerJobIdAndStatusOrderByIdDesc(Long crawlerJobId, String status, Pageable pageable);
    Page<CrawlerRun> findAllByOrderByIdDesc(Pageable pageable);
    long countByStatus(String status);
}
