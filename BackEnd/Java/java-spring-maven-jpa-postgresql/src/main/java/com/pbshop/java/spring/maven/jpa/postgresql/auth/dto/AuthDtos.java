package com.pbshop.java.spring.maven.jpa.postgresql.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record SignupRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8) String password,
            @NotBlank @Size(min = 2, max = 20) String name,
            @NotBlank String phone
    ) {
    }

    public record VerifyEmailRequest(@Email @NotBlank String email, @NotBlank String code) {
    }

    public record ResendVerificationRequest(@Email @NotBlank String email) {
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record PasswordResetRequest(@Email @NotBlank String email, @NotBlank String phone) {
    }

    public record PasswordResetVerifyRequest(@Email @NotBlank String email, @NotBlank String code) {
    }

    public record PasswordResetConfirmRequest(@NotBlank String resetToken, @NotBlank @Size(min = 8) String newPassword) {
    }

    public record SocialCompleteRequest(@NotBlank String phone, @NotBlank String nickname) {
    }

    public record SocialLinkRequest(
            @NotBlank @Pattern(regexp = "google|naver|kakao|facebook|instagram") String provider,
            @NotBlank String socialToken
    ) {
    }
}
