package com.pbshop.java.spring.maven.jpa.postgresql.badge;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    List<Badge> findAllByOrderByIdAsc();
}
