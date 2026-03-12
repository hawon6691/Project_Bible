package com.pbshop.springshop.friend;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendBlockRepository extends JpaRepository<FriendBlock, Long> {
    boolean existsByUserIdAndBlockedUserId(Long userId, Long blockedUserId);
    Optional<FriendBlock> findByUserIdAndBlockedUserId(Long userId, Long blockedUserId);
}
