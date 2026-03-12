package com.pbshop.springshop.news;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsProductRepository extends JpaRepository<NewsProduct, Long> {
    @EntityGraph(attributePaths = {"product"})
    List<NewsProduct> findByNewsIdOrderByIdAsc(Long newsId);
    void deleteByNewsId(Long newsId);
}
