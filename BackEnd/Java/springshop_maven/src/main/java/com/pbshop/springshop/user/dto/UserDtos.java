package com.pbshop.springshop.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class UserDtos {

    private UserDtos() {
    }

    public record UpdateMeRequest(
            @Size(min = 2, max = 20) String name,
            String phone,
            @Size(min = 8) String password
    ) {
    }

    public record UpdateProfileRequest(
            @Size(min = 2, max = 20) String nickname,
            @Size(max = 1000) String bio
    ) {
    }

    public record UpdateUserStatusRequest(
            @Pattern(regexp = "ACTIVE|INACTIVE|SUSPENDED|DELETED") String status
    ) {
    }
}
