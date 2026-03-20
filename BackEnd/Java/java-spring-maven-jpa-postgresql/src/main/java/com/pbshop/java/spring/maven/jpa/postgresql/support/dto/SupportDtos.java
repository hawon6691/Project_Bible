package com.pbshop.java.spring.maven.jpa.postgresql.support.dto;

import jakarta.validation.constraints.NotBlank;

public final class SupportDtos {

    private SupportDtos() {
    }

    public record CreateTicketRequest(
            @NotBlank String category,
            @NotBlank String title,
            @NotBlank String content
    ) {
    }

    public record ReplyTicketRequest(
            @NotBlank String content
    ) {
    }

    public record UpdateTicketStatusRequest(
            @NotBlank String status
    ) {
    }
}
