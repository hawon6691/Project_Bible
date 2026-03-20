package com.pbshop.java.spring.maven.jpa.postgresql.pcbuilder;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PcBuildPartRepository extends JpaRepository<PcBuildPart, Long> {
    @EntityGraph(attributePaths = {"product", "product.category"})
    List<PcBuildPart> findByPcBuildIdOrderByIdAsc(Long pcBuildId);
    Optional<PcBuildPart> findByIdAndPcBuildId(Long id, Long pcBuildId);
}
