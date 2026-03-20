package com.pbshop.java.spring.maven.jpa.postgresql.chat;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<ChatMessage> findByRoomIdOrderByIdAsc(Long roomId);
}
