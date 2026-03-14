package com.pbshop.springshop.support;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportTicketReplyRepository extends JpaRepository<SupportTicketReply, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<SupportTicketReply> findByTicketIdOrderByIdAsc(Long ticketId);
}
