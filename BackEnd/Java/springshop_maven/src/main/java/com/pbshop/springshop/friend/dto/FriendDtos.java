package com.pbshop.springshop.friend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class FriendDtos {
    private FriendDtos() {
    }
    public record RequestFriendRequest(@NotNull Long userId) { }
    public record BlockUserRequest(@NotNull Long userId) { }
    public record RejectMappingRequest(@NotBlank String reason) { }
}
