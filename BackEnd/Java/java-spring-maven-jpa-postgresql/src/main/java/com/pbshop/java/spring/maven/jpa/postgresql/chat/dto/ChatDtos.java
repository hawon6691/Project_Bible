package com.pbshop.java.spring.maven.jpa.postgresql.chat.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public final class ChatDtos {

    private ChatDtos() {
    }

    public record CreateRoomRequest(
            String title,
            List<Long> participantUserIds
    ) {
    }

    public record SendMessageRequest(
            @NotBlank String content
    ) {
    }
}
