package com.pbshop.springshop.friend;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendActivityRepository extends JpaRepository<FriendActivity, Long> {
    @EntityGraph(attributePaths = {"user"})
    List<FriendActivity> findByUserIdInOrderByIdDesc(List<Long> userIds);
}
