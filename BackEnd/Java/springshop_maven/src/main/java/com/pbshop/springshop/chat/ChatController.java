package com.pbshop.springshop.chat;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.chat.dto.ChatDtos.CreateRoomRequest;
import com.pbshop.springshop.chat.dto.ChatDtos.SendMessageRequest;
import com.pbshop.springshop.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/rooms")
    public ApiResponse<List<Map<String, Object>>> getRooms(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ApiResponse.success(chatService.getRooms(principal));
    }

    @PostMapping("/rooms")
    public ApiResponse<Map<String, Object>> createRoom(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestBody CreateRoomRequest request
    ) {
        return ApiResponse.success(chatService.createRoom(principal, request));
    }

    @PostMapping("/rooms/{id}/join")
    public ApiResponse<Map<String, Object>> joinRoom(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(chatService.joinRoom(principal, id));
    }

    @GetMapping("/rooms/{id}/messages")
    public ApiResponse<List<Map<String, Object>>> getMessages(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(chatService.getMessages(principal, id));
    }

    @PostMapping("/rooms/{id}/messages")
    public ApiResponse<Map<String, Object>> sendMessage(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody SendMessageRequest request
    ) {
        return ApiResponse.success(chatService.sendMessage(principal, id, request));
    }
}
