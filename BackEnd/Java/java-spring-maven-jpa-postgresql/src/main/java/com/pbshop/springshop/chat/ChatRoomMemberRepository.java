package com.pbshop.springshop.chat;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<ChatRoomMember> findByRoomIdOrderByIdAsc(Long roomId);
}
