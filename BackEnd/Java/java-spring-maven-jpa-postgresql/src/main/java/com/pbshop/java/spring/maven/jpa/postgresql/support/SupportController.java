package com.pbshop.java.spring.maven.jpa.postgresql.support;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;
import com.pbshop.java.spring.maven.jpa.postgresql.support.dto.SupportDtos.CreateTicketRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.support.dto.SupportDtos.ReplyTicketRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.support.dto.SupportDtos.UpdateTicketStatusRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}")
public class SupportController {

    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @GetMapping("/support/tickets")
    public ApiResponse<Map<String, Object>> getMyTickets(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.success(supportService.getMyTickets(principal, page, limit));
    }

    @PostMapping("/support/tickets")
    public ApiResponse<Map<String, Object>> createTicket(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody CreateTicketRequest request
    ) {
        return ApiResponse.success(supportService.createTicket(principal, request));
    }

    @GetMapping("/support/tickets/{id}")
    public ApiResponse<Map<String, Object>> getTicketDetail(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(supportService.getTicketDetail(principal, id));
    }

    @PostMapping("/support/tickets/{id}/reply")
    public ApiResponse<Map<String, Object>> replyTicket(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ReplyTicketRequest request
    ) {
        return ApiResponse.success(supportService.replyTicket(principal, id, request));
    }

    @GetMapping("/admin/support/tickets")
    public ApiResponse<Map<String, Object>> getAdminTickets(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.success(supportService.getAdminTickets(principal, page, limit));
    }

    @PatchMapping("/admin/support/tickets/{id}/status")
    public ApiResponse<Map<String, Object>> updateTicketStatus(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request
    ) {
        return ApiResponse.success(supportService.updateStatus(principal, id, request));
    }
}
