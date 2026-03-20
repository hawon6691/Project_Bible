package com.pbshop.java.spring.maven.jpa.postgresql.friend;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.api.ApiResponse;
import com.pbshop.java.spring.maven.jpa.postgresql.friend.dto.FriendDtos.BlockUserRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.friend.dto.FriendDtos.RequestFriendRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/friends")
public class FriendController {
    private final FriendService friendService;
    public FriendController(FriendService friendService) { this.friendService = friendService; }

    @PostMapping("/requests")
    public ApiResponse<Map<String, Object>> request(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody RequestFriendRequest request) {
        return ApiResponse.success(friendService.requestFriend(principal, request.userId()));
    }
    @PostMapping("/requests/{id}/accept")
    public ApiResponse<Map<String, Object>> accept(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id) {
        return ApiResponse.success(friendService.accept(principal, id));
    }
    @PostMapping("/requests/{id}/reject")
    public ApiResponse<Map<String, Object>> reject(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id) {
        return ApiResponse.success(friendService.reject(principal, id));
    }
    @GetMapping
    public ApiResponse<Map<String, Object>> list(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(friendService.getFriends(principal));
    }
    @GetMapping("/requests/received")
    public ApiResponse<Map<String, Object>> received(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(friendService.getReceived(principal));
    }
    @GetMapping("/requests/sent")
    public ApiResponse<Map<String, Object>> sent(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(friendService.getSent(principal));
    }
    @GetMapping("/feed")
    public ApiResponse<Map<String, Object>> feed(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(friendService.getFeed(principal));
    }
    @PostMapping("/blocks")
    public ApiResponse<Map<String, Object>> block(@AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody BlockUserRequest request) {
        return ApiResponse.success(friendService.block(principal, request.userId()));
    }
    @DeleteMapping("/blocks/{userId}")
    public ApiResponse<Map<String, Object>> unblock(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long userId) {
        return ApiResponse.success(friendService.unblock(principal, userId));
    }
    @DeleteMapping("/{userId}")
    public ApiResponse<Map<String, Object>> remove(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long userId) {
        return ApiResponse.success(friendService.remove(principal, userId));
    }
}
