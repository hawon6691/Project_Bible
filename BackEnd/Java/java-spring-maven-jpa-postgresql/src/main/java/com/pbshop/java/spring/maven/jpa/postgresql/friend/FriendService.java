package com.pbshop.java.spring.maven.jpa.postgresql.friend;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.user.User;
import com.pbshop.java.spring.maven.jpa.postgresql.user.UserRepository;

@Service
@Transactional
public class FriendService {
    private final FriendshipRepository friendshipRepository;
    private final FriendBlockRepository friendBlockRepository;
    private final FriendActivityRepository friendActivityRepository;
    private final UserRepository userRepository;

    public FriendService(FriendshipRepository friendshipRepository, FriendBlockRepository friendBlockRepository,
            FriendActivityRepository friendActivityRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.friendBlockRepository = friendBlockRepository;
        this.friendActivityRepository = friendActivityRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> requestFriend(AuthenticatedUserPrincipal principal, Long userId) {
        User actor = requireCurrentUser(principal);
        if (actor.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "본인에게 친구 요청을 보낼 수 없습니다.");
        }
        User target = requireUser(userId);
        if (friendBlockRepository.existsByUserIdAndBlockedUserId(actor.getId(), userId)
                || friendBlockRepository.existsByUserIdAndBlockedUserId(userId, actor.getId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "차단 관계에서는 친구 요청을 보낼 수 없습니다.");
        }
        friendshipRepository.findByRequesterIdAndAddresseeId(actor.getId(), target.getId()).orElseGet(() -> {
            Friendship friendship = new Friendship();
            friendship.setRequester(actor);
            friendship.setAddressee(target);
            friendship.setStatus("PENDING");
            return friendshipRepository.save(friendship);
        });
        return Map.of("message", "친구 요청을 보냈습니다.");
    }

    public Map<String, Object> accept(AuthenticatedUserPrincipal principal, Long friendshipId) {
        User actor = requireCurrentUser(principal);
        Friendship friendship = findFriendship(friendshipId);
        if (!friendship.getAddressee().getId().equals(actor.getId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "친구 요청을 찾을 수 없습니다.");
        }
        friendship.setStatus("ACCEPTED");
        friendActivityRepository.save(activity(actor, "FRIEND_ACCEPTED", "친구 요청을 수락했습니다."));
        return Map.of("message", "친구 요청을 수락했습니다.");
    }

    public Map<String, Object> reject(AuthenticatedUserPrincipal principal, Long friendshipId) {
        User actor = requireCurrentUser(principal);
        Friendship friendship = findFriendship(friendshipId);
        if (!friendship.getAddressee().getId().equals(actor.getId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "친구 요청을 찾을 수 없습니다.");
        }
        friendship.setStatus("REJECTED");
        return Map.of("message", "친구 요청을 거절했습니다.");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getFriends(AuthenticatedUserPrincipal principal) {
        User actor = requireCurrentUser(principal);
        List<Map<String, Object>> items = friendshipRepository.findByStatusOrderByIdDesc("ACCEPTED").stream()
                .filter(friendship -> friendship.getRequester().getId().equals(actor.getId())
                        || friendship.getAddressee().getId().equals(actor.getId()))
                .map(friendship -> toFriendResponse(actor, friendship))
                .toList();
        return Map.of("items", items, "pagination", pagination(items.size()));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getReceived(AuthenticatedUserPrincipal principal) {
        User actor = requireCurrentUser(principal);
        List<Map<String, Object>> items = friendshipRepository.findByAddresseeIdAndStatusOrderByIdDesc(actor.getId(), "PENDING")
                .stream().map(this::toRequestResponse).toList();
        return Map.of("items", items, "pagination", pagination(items.size()));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSent(AuthenticatedUserPrincipal principal) {
        User actor = requireCurrentUser(principal);
        List<Map<String, Object>> items = friendshipRepository.findByRequesterIdAndStatusOrderByIdDesc(actor.getId(), "PENDING")
                .stream().map(this::toRequestResponse).toList();
        return Map.of("items", items, "pagination", pagination(items.size()));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getFeed(AuthenticatedUserPrincipal principal) {
        User actor = requireCurrentUser(principal);
        List<Long> friendIds = friendshipRepository.findByStatusOrderByIdDesc("ACCEPTED").stream()
                .filter(friendship -> friendship.getRequester().getId().equals(actor.getId())
                        || friendship.getAddressee().getId().equals(actor.getId()))
                .flatMap(friendship -> List.of(friendship.getRequester().getId(), friendship.getAddressee().getId()).stream())
                .filter(id -> !id.equals(actor.getId()))
                .distinct()
                .toList();
        List<Map<String, Object>> items = friendIds.isEmpty() ? List.of()
                : friendActivityRepository.findByUserIdInOrderByIdDesc(friendIds).stream().map(this::toActivityResponse).toList();
        return Map.of("items", items, "pagination", pagination(items.size()));
    }

    public Map<String, Object> block(AuthenticatedUserPrincipal principal, Long userId) {
        User actor = requireCurrentUser(principal);
        User blocked = requireUser(userId);
        friendBlockRepository.findByUserIdAndBlockedUserId(actor.getId(), blocked.getId()).orElseGet(() -> {
            FriendBlock block = new FriendBlock();
            block.setUser(actor);
            block.setBlockedUser(blocked);
            return friendBlockRepository.save(block);
        });
        friendshipRepository.findByRequesterIdAndAddresseeId(actor.getId(), blocked.getId()).ifPresent(friendshipRepository::delete);
        friendshipRepository.findByRequesterIdAndAddresseeId(blocked.getId(), actor.getId()).ifPresent(friendshipRepository::delete);
        return Map.of("message", "사용자를 차단했습니다.");
    }

    public Map<String, Object> unblock(AuthenticatedUserPrincipal principal, Long userId) {
        User actor = requireCurrentUser(principal);
        FriendBlock block = friendBlockRepository.findByUserIdAndBlockedUserId(actor.getId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "차단 대상을 찾을 수 없습니다."));
        friendBlockRepository.delete(block);
        return Map.of("message", "사용자 차단을 해제했습니다.");
    }

    public Map<String, Object> remove(AuthenticatedUserPrincipal principal, Long userId) {
        User actor = requireCurrentUser(principal);
        friendshipRepository.findByRequesterIdAndAddresseeId(actor.getId(), userId).ifPresent(friendshipRepository::delete);
        friendshipRepository.findByRequesterIdAndAddresseeId(userId, actor.getId()).ifPresent(friendshipRepository::delete);
        return Map.of("message", "친구를 삭제했습니다.");
    }

    private Friendship findFriendship(Long id) {
        return friendshipRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "친구 요청을 찾을 수 없습니다."));
    }

    private FriendActivity activity(User user, String type, String message) {
        FriendActivity activity = new FriendActivity();
        activity.setUser(user);
        activity.setType(type);
        activity.setMessage(message);
        return activity;
    }

    private Map<String, Object> toFriendResponse(User actor, Friendship friendship) {
        User friend = friendship.getRequester().getId().equals(actor.getId()) ? friendship.getAddressee() : friendship.getRequester();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", friendship.getId());
        response.put("friend", Map.of("id", friend.getId(), "name", friend.getName(), "nickname", friend.getName()));
        response.put("status", friendship.getStatus());
        response.put("createdAt", friendship.getCreatedAt() == null ? null : friendship.getCreatedAt().toString());
        return response;
    }

    private Map<String, Object> toRequestResponse(Friendship friendship) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", friendship.getId());
        response.put("requesterId", friendship.getRequester().getId());
        response.put("addresseeId", friendship.getAddressee().getId());
        response.put("status", friendship.getStatus());
        response.put("createdAt", friendship.getCreatedAt() == null ? null : friendship.getCreatedAt().toString());
        return response;
    }

    private Map<String, Object> toActivityResponse(FriendActivity activity) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", activity.getId());
        response.put("userId", activity.getUser().getId());
        response.put("type", activity.getType());
        response.put("message", activity.getMessage());
        response.put("createdAt", activity.getCreatedAt() == null ? null : activity.getCreatedAt().toString());
        return response;
    }

    private Map<String, Object> pagination(int total) {
        return Map.of("page", 1, "limit", total == 0 ? 20 : total, "total", total, "totalPages", total == 0 ? 0 : 1);
    }

    private User requireCurrentUser(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return requireUser(principal.userId());
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
