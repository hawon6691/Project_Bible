package com.pbshop.java.spring.maven.jpa.postgresql.spec;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecDefinitionRepository extends JpaRepository<SpecDefinition, Long> {

    @EntityGraph(attributePaths = {"category"})
    List<SpecDefinition> findByCategoryIdOrderBySortOrderAscIdAsc(Long categoryId);

    @EntityGraph(attributePaths = {"category"})
    List<SpecDefinition> findAllByOrderBySortOrderAscIdAsc();

    boolean existsByCategoryIdAndSpecKey(Long categoryId, String specKey);

    boolean existsByCategoryIdAndSpecKeyAndIdNot(Long categoryId, String specKey, Long id);
}
