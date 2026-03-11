package com.pbshop.springshop.product;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category"})
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findDetailById(@Param("id") Long id);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @EntityGraph(attributePaths = {"category", "priceEntries", "priceEntries.seller"})
    @Query("""
            select distinct p from Product p
            left join p.priceEntries pe
            where (:categoryId is null or p.category.id = :categoryId)
              and (:search is null or lower(p.name) like lower(concat('%', :search, '%'))
                   or lower(coalesce(p.brand, '')) like lower(concat('%', :search, '%')))
              and (:minPrice is null or pe.price >= :minPrice)
              and (:maxPrice is null or pe.price <= :maxPrice)
            """)
    Page<Product> search(
            @Nullable @Param("categoryId") Long categoryId,
            @Nullable @Param("search") String search,
            @Nullable @Param("minPrice") BigDecimal minPrice,
            @Nullable @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}
