package com.pbshop.springshop.recommendation;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    @EntityGraph(attributePaths = {"product", "product.category", "targetUser"})
    List<Recommendation> findAll(Sort sort);

    @EntityGraph(attributePaths = {"product", "product.category"})
    List<Recommendation> findByActiveTrueAndTargetTypeOrderByScoreDescIdDesc(String targetType);

    @EntityGraph(attributePaths = {"product", "product.category"})
    List<Recommendation> findByActiveTrueAndTargetUserIdOrderByScoreDescIdDesc(Long targetUserId);
}
