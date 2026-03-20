package com.pbshop.java.spring.maven.jpa.postgresql.deal;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DealRepository extends JpaRepository<Deal, Long> {

    @EntityGraph(attributePaths = {"product", "product.category"})
    Optional<Deal> findWithProductById(Long id);

    @EntityGraph(attributePaths = {"product", "product.category"})
    List<Deal> findAll(Sort sort);

    @EntityGraph(attributePaths = {"product", "product.category"})
    @Query("""
            select d from Deal d
            where d.status = 'ACTIVE'
              and d.startAt <= :now
              and d.endAt >= :now
              and (:type is null or upper(d.type) = upper(:type))
            order by d.startAt desc, d.id desc
            """)
    List<Deal> findActiveDeals(@Param("now") OffsetDateTime now, @Param("type") String type);
}
