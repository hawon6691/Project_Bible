package com.pbshop.springshop.automotive;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutoTrimRepository extends JpaRepository<AutoTrim, Long> {
    @EntityGraph(attributePaths = {"autoModel"})
    List<AutoTrim> findByAutoModelIdOrderByIdAsc(Long autoModelId);
}
