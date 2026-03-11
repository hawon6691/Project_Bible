package com.pbshop.springshop.product;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PriceEntryRepository extends JpaRepository<PriceEntry, Long> {

    @EntityGraph(attributePaths = {"seller"})
    List<PriceEntry> findByProductIdOrderByPriceAsc(Long productId);

    @EntityGraph(attributePaths = {"seller", "product"})
    Optional<PriceEntry> findDetailById(Long id);

    @Query("""
            select pe from PriceEntry pe
            where pe.product.id = :productId
              and pe.checkedAt >= :from
            order by pe.checkedAt asc, pe.price asc
            """)
    List<PriceEntry> findHistoryByProductIdSince(@Param("productId") Long productId, @Param("from") OffsetDateTime from);
}
