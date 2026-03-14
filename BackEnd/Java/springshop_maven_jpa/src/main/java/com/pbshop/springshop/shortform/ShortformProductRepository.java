package com.pbshop.springshop.shortform;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortformProductRepository extends JpaRepository<ShortformProduct, Long> {
    @EntityGraph(attributePaths = {"product"})
    List<ShortformProduct> findByShortformIdOrderByIdAsc(Long shortformId);
    void deleteByShortformId(Long shortformId);
}
