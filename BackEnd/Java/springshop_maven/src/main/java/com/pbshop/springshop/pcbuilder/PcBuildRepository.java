package com.pbshop.springshop.pcbuilder;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PcBuildRepository extends JpaRepository<PcBuild, Long> {
    @EntityGraph(attributePaths = {"user"})
    List<PcBuild> findByUserIdOrderByIdDesc(Long userId);
    @EntityGraph(attributePaths = {"user"})
    List<PcBuild> findAllByOrderByViewCountDescIdDesc();
    @EntityGraph(attributePaths = {"user"})
    Optional<PcBuild> findByShareCode(String shareCode);
    @EntityGraph(attributePaths = {"user"})
    Optional<PcBuild> findByIdAndUserId(Long id, Long userId);
}
