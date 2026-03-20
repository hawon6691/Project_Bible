package com.pbshop.springshop.shortform;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortformRepository extends JpaRepository<Shortform, Long> {
    @EntityGraph(attributePaths = {"user"})
    List<Shortform> findAllByOrderByIdDesc();
    @EntityGraph(attributePaths = {"user"})
    List<Shortform> findByUserIdOrderByIdDesc(Long userId);
}
