package com.pbshop.java.spring.maven.jpa.postgresql.shortform;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortformCommentRepository extends JpaRepository<ShortformComment, Long> {
    @EntityGraph(attributePaths = {"user"})
    List<ShortformComment> findByShortformIdOrderByIdAsc(Long shortformId);
    long countByShortformId(Long shortformId);
}
