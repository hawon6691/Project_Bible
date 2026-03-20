package com.pbshop.java.spring.maven.jpa.postgresql.shortform;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortformLikeRepository extends JpaRepository<ShortformLike, Long> {
    Optional<ShortformLike> findByShortformIdAndUserId(Long shortformId, Long userId);
    long countByShortformId(Long shortformId);
}
