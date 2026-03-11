package com.pbshop.springshop.product;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceEntryRepository extends JpaRepository<PriceEntry, Long> {

    @EntityGraph(attributePaths = {"seller"})
    List<PriceEntry> findByProductIdOrderByPriceAsc(Long productId);
}
