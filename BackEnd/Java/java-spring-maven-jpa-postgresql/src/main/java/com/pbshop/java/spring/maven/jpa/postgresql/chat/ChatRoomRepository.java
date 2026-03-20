package com.pbshop.java.spring.maven.jpa.postgresql.chat;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @EntityGraph(attributePaths = {"createdBy"})
    @Query("""
            select distinct r
            from ChatRoom r
            join ChatRoomMember m on m.room.id = r.id
            where m.user.id = :userId
            order by r.id desc
            """)
    List<ChatRoom> findAllByMemberUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"createdBy"})
    Optional<ChatRoom> findById(Long id);
}
