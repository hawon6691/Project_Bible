package com.pbshop.springshop.community;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<PostComment> findByPostIdOrderByIdAsc(Long postId);

    long countByPostId(Long postId);
}
