package com.pbshop.springshop.activity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    List<SearchHistory> findByUserIdOrderByIdDesc(Long userId);

    long countByUserId(Long userId);

    void deleteByUserId(Long userId);
}
