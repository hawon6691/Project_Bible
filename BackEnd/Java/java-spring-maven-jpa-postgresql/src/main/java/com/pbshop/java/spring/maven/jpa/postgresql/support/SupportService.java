package com.pbshop.java.spring.maven.jpa.postgresql.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.support.dto.SupportDtos.CreateTicketRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.support.dto.SupportDtos.ReplyTicketRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.support.dto.SupportDtos.UpdateTicketStatusRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.user.User;
import com.pbshop.java.spring.maven.jpa.postgresql.user.UserRepository;

@Service
@Transactional(readOnly = true)
public class SupportService {

    private final SupportTicketRepository supportTicketRepository;
    private final SupportTicketReplyRepository supportTicketReplyRepository;
    private final UserRepository userRepository;

    public SupportService(
            SupportTicketRepository supportTicketRepository,
            SupportTicketReplyRepository supportTicketReplyRepository,
            UserRepository userRepository
    ) {
        this.supportTicketRepository = supportTicketRepository;
        this.supportTicketReplyRepository = supportTicketReplyRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getMyTickets(AuthenticatedUserPrincipal principal, int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), limit, Sort.by(Sort.Direction.DESC, "id"));
        Page<SupportTicket> ticketPage = supportTicketRepository.findByUserIdOrderByIdDesc(principal.userId(), pageable);
        return toPageResponse(ticketPage);
    }

    @Transactional
    public Map<String, Object> createTicket(AuthenticatedUserPrincipal principal, CreateTicketRequest request) {
        SupportTicket ticket = new SupportTicket();
        ticket.setUser(getUser(principal.userId()));
        ticket.setCategory(request.category());
        ticket.setTitle(request.title());
        ticket.setContent(request.content());
        ticket.setStatus("OPEN");
        return toTicketResponse(supportTicketRepository.save(ticket));
    }

    public Map<String, Object> getTicketDetail(AuthenticatedUserPrincipal principal, Long ticketId) {
        SupportTicket ticket = getTicket(ticketId);
        requireOwnerOrAdmin(principal, ticket.getUser().getId());
        Map<String, Object> response = new LinkedHashMap<>(toTicketResponse(ticket));
        response.put("replies", supportTicketReplyRepository.findByTicketIdOrderByIdAsc(ticketId).stream()
                .map(this::toReplyResponse)
                .toList());
        return response;
    }

    @Transactional
    public Map<String, Object> replyTicket(AuthenticatedUserPrincipal principal, Long ticketId, ReplyTicketRequest request) {
        SupportTicket ticket = getTicket(ticketId);
        requireOwnerOrAdmin(principal, ticket.getUser().getId());
        SupportTicketReply reply = new SupportTicketReply();
        reply.setTicket(ticket);
        reply.setUser(getUser(principal.userId()));
        reply.setContent(request.content());
        reply.setAdminReply("ADMIN".equalsIgnoreCase(principal.role()));
        SupportTicketReply saved = supportTicketReplyRepository.save(reply);
        if ("OPEN".equalsIgnoreCase(ticket.getStatus()) && "ADMIN".equalsIgnoreCase(principal.role())) {
            ticket.setStatus("ANSWERED");
            supportTicketRepository.save(ticket);
        }
        return toReplyResponse(saved);
    }

    public Map<String, Object> getAdminTickets(AuthenticatedUserPrincipal principal, int page, int limit) {
        requireAdmin(principal);
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), limit, Sort.by(Sort.Direction.DESC, "id"));
        Page<SupportTicket> ticketPage = supportTicketRepository.findAllByOrderByIdDesc(pageable);
        return toPageResponse(ticketPage);
    }

    @Transactional
    public Map<String, Object> updateStatus(AuthenticatedUserPrincipal principal, Long ticketId, UpdateTicketStatusRequest request) {
        requireAdmin(principal);
        SupportTicket ticket = getTicket(ticketId);
        ticket.setStatus(request.status());
        return toTicketResponse(supportTicketRepository.save(ticket));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private SupportTicket getTicket(Long ticketId) {
        return supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "고객센터 문의가 존재하지 않습니다."));
    }

    private void requireAdmin(AuthenticatedUserPrincipal principal) {
        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }

    private void requireOwnerOrAdmin(AuthenticatedUserPrincipal principal, Long ownerId) {
        if ("ADMIN".equalsIgnoreCase(principal.role())) {
            return;
        }
        if (!principal.userId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private Map<String, Object> toPageResponse(Page<SupportTicket> ticketPage) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", ticketPage.getContent().stream().map(this::toTicketResponse).toList());
        response.put("page", ticketPage.getNumber() + 1);
        response.put("limit", ticketPage.getSize());
        response.put("total", ticketPage.getTotalElements());
        response.put("totalPages", ticketPage.getTotalPages());
        return response;
    }

    private Map<String, Object> toTicketResponse(SupportTicket ticket) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", ticket.getId());
        response.put("userId", ticket.getUser().getId());
        response.put("userName", ticket.getUser().getName());
        response.put("category", ticket.getCategory());
        response.put("title", ticket.getTitle());
        response.put("content", ticket.getContent());
        response.put("status", ticket.getStatus());
        response.put("createdAt", ticket.getCreatedAt());
        return response;
    }

    private Map<String, Object> toReplyResponse(SupportTicketReply reply) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", reply.getId());
        response.put("ticketId", reply.getTicket().getId());
        response.put("userId", reply.getUser().getId());
        response.put("userName", reply.getUser().getName());
        response.put("content", reply.getContent());
        response.put("adminReply", reply.isAdminReply());
        response.put("createdAt", reply.getCreatedAt());
        return response;
    }
}
