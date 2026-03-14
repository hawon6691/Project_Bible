package com.pbshop.springshop.news;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Long> {
    @EntityGraph(attributePaths = {"category", "author"})
    List<News> findAllByOrderByIdDesc();
    @EntityGraph(attributePaths = {"category", "author"})
    List<News> findByCategoryIdOrderByIdDesc(Long categoryId);
}
