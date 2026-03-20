package com.pbshop.java.spring.maven.jpa.postgresql.automotive;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutoOptionRepository extends JpaRepository<AutoOption, Long> {
    @EntityGraph(attributePaths = {"autoTrim", "autoTrim.autoModel"})
    List<AutoOption> findByAutoTrimIdOrderByIdAsc(Long autoTrimId);
    @EntityGraph(attributePaths = {"autoTrim", "autoTrim.autoModel"})
    List<AutoOption> findByIdIn(List<Long> ids);
}
