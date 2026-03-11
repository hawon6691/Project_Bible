package com.pbshop.springshop.chat;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.chat.dto.ChatDtos.CreateRoomRequest;
import com.pbshop.springshop.chat.dto.ChatDtos.SendMessageRequest;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@Service
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public ChatService(
            ChatRoomRepository chatRoomRepository,
            ChatRoomMemberRepository chatRoomMemberRepository,
            ChatMessageRepository chatMessageRepository,
            UserRepository userRepository
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
    }

    public List<Map<String, Object>> getRooms(AuthenticatedUserPrincipal principal) {
        return chatRoomRepository.findAllByMemberUserId(principal.userId()).stream()
                .map(this::toRoomResponse)
                .toList();
    }

    @Transactional
    public Map<String, Object> createRoom(AuthenticatedUserPrincipal principal, CreateRoomRequest request) {
        User creator = getUser(principal.userId());
        ChatRoom room = new ChatRoom();
        room.setCreatedBy(creator);
        room.setTitle(request.title());

        Set<Long> participantIds = new LinkedHashSet<>();
        participantIds.add(principal.userId());
        if (request.participantUserIds() != null) {
            participantIds.addAll(request.participantUserIds());
        }
        room.setRoomType(participantIds.size() > 2 ? "GROUP" : "DIRECT");
        ChatRoom savedRoom = chatRoomRepository.save(room);

        for (Long participantId : participantIds) {
            ChatRoomMember member = new ChatRoomMember();
            member.setRoom(savedRoom);
            member.setUser(getUser(participantId));
            member.setJoinedAt(OffsetDateTime.now());
            chatRoomMemberRepository.save(member);
        }

        return toRoomResponse(savedRoom);
    }

    @Transactional
    public Map<String, Object> joinRoom(AuthenticatedUserPrincipal principal, Long roomId) {
        ChatRoom room = getRoom(roomId);
        return chatRoomMemberRepository.findByRoomIdAndUserId(roomId, principal.userId())
                .map(existing -> {
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("joined", true);
                    response.put("room", toRoomResponse(room));
                    return response;
                })
                .orElseGet(() -> {
                    ChatRoomMember member = new ChatRoomMember();
                    member.setRoom(room);
                    member.setUser(getUser(principal.userId()));
                    member.setJoinedAt(OffsetDateTime.now());
                    chatRoomMemberRepository.save(member);
                    Map<String, Object> response = new LinkedHashMap<>();
                    response.put("joined", true);
                    response.put("room", toRoomResponse(room));
                    return response;
                });
    }

    public List<Map<String, Object>> getMessages(AuthenticatedUserPrincipal principal, Long roomId) {
        ensureMember(roomId, principal.userId());
        return chatMessageRepository.findByRoomIdOrderByIdAsc(roomId).stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Transactional
    public Map<String, Object> sendMessage(
            AuthenticatedUserPrincipal principal,
            Long roomId,
            SendMessageRequest request
    ) {
        ChatRoom room = ensureMember(roomId, principal.userId());
        ChatMessage message = new ChatMessage();
        message.setRoom(room);
        message.setUser(getUser(principal.userId()));
        message.setContent(request.content());
        return toMessageResponse(chatMessageRepository.save(message));
    }

    private ChatRoom ensureMember(Long roomId, Long userId) {
        ChatRoom room = getRoom(roomId);
        if (chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId).isEmpty()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return room;
    }

    private ChatRoom getRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private Map<String, Object> toRoomResponse(ChatRoom room) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", room.getId());
        response.put("title", room.getTitle());
        response.put("roomType", room.getRoomType());
        response.put("status", room.getStatus());
        response.put("createdByUserId", room.getCreatedBy().getId());
        response.put("members", chatRoomMemberRepository.findByRoomIdOrderByIdAsc(room.getId()).stream()
                .map(member -> Map.of(
                        "userId", member.getUser().getId(),
                        "userName", member.getUser().getName(),
                        "joinedAt", member.getJoinedAt()
                ))
                .toList());
        response.put("createdAt", room.getCreatedAt());
        return response;
    }

    private Map<String, Object> toMessageResponse(ChatMessage message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", message.getId());
        response.put("roomId", message.getRoom().getId());
        response.put("userId", message.getUser().getId());
        response.put("userName", message.getUser().getName());
        response.put("content", message.getContent());
        response.put("createdAt", message.getCreatedAt());
        return response;
    }
}
