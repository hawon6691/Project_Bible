package com.pbshop.java.spring.maven.jpa.postgresql.badge;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    @EntityGraph(attributePaths = {"badge"})
    List<UserBadge> findByUserIdOrderByGrantedAtDescIdDesc(Long userId);

    Optional<UserBadge> findByBadgeIdAndUserId(Long badgeId, Long userId);

    long countByBadgeId(Long badgeId);
}
