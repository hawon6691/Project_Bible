package com.pbshop.java.spring.maven.jpa.postgresql.friend;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @EntityGraph(attributePaths = {"requester", "addressee"})
    List<Friendship> findByStatusOrderByIdDesc(String status);
    @EntityGraph(attributePaths = {"requester", "addressee"})
    List<Friendship> findByAddresseeIdAndStatusOrderByIdDesc(Long addresseeId, String status);
    @EntityGraph(attributePaths = {"requester", "addressee"})
    List<Friendship> findByRequesterIdAndStatusOrderByIdDesc(Long requesterId, String status);
    @EntityGraph(attributePaths = {"requester", "addressee"})
    Optional<Friendship> findByRequesterIdAndAddresseeId(Long requesterId, Long addresseeId);
}
