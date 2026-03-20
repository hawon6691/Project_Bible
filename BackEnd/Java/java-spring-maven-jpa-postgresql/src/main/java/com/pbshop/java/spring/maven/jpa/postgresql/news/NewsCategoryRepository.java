package com.pbshop.java.spring.maven.jpa.postgresql.news;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsCategoryRepository extends JpaRepository<NewsCategory, Long> {
    List<NewsCategory> findAllByOrderByIdAsc();
}
