package com.pbshop.springshop.community;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"board", "user"})
    @Query("""
            select p from Post p
            where p.board.id = :boardId
              and (:search is null or lower(p.title) like lower(concat('%', :search, '%'))
                   or lower(p.content) like lower(concat('%', :search, '%')))
            """)
    Page<Post> findBoardPosts(@Param("boardId") Long boardId, @Param("search") String search, Pageable pageable);

    @EntityGraph(attributePaths = {"board", "user"})
    Optional<Post> findById(Long id);
}
