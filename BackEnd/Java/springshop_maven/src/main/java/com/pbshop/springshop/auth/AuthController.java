package com.pbshop.springshop.auth;

import java.net.URI;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.dto.AuthDtos.LoginRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.PasswordResetConfirmRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.PasswordResetRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.PasswordResetVerifyRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.RefreshRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.ResendVerificationRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.SignupRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.SocialCompleteRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.SocialLinkRequest;
import com.pbshop.springshop.auth.dto.AuthDtos.VerifyEmailRequest;
import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ApiResponse<Map<String, Object>> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success(authService.signup(request));
    }

    @PostMapping("/verify-email")
    public ApiResponse<Map<String, Object>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return ApiResponse.success(authService.verifyEmail(request));
    }

    @PostMapping("/resend-verification")
    public ApiResponse<Map<String, Object>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        return ApiResponse.success(authService.resendVerification(request));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logout(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(authService.logout(principal));
    }

    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    @PostMapping("/password-reset/request")
    public ApiResponse<Map<String, Object>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        return ApiResponse.success(authService.requestPasswordReset(request));
    }

    @PostMapping("/password-reset/verify")
    public ApiResponse<Map<String, Object>> verifyPasswordReset(@Valid @RequestBody PasswordResetVerifyRequest request) {
        return ApiResponse.success(authService.verifyPasswordReset(request));
    }

    @PostMapping("/password-reset/confirm")
    public ApiResponse<Map<String, Object>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        return ApiResponse.success(authService.confirmPasswordReset(request));
    }

    @GetMapping("/login/{provider}")
    public ResponseEntity<Void> socialLogin(@PathVariable String provider) {
        URI location = authService.socialLoginRedirect(provider);
        return ResponseEntity.status(302).location(location).build();
    }

    @GetMapping("/callback/{provider}")
    public ApiResponse<Map<String, Object>> socialCallback(@PathVariable String provider, @RequestParam(required = false) String code) {
        return ApiResponse.success(authService.socialCallback(provider, code));
    }

    @PostMapping("/social/complete")
    public ApiResponse<Map<String, Object>> socialComplete(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody SocialCompleteRequest request
    ) {
        return ApiResponse.success(authService.socialComplete(principal, request));
    }

    @PostMapping("/social/link")
    public ApiResponse<Map<String, Object>> socialLink(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody SocialLinkRequest request
    ) {
        return ApiResponse.success(authService.socialLink(principal, request));
    }

    @DeleteMapping("/social/unlink/{provider}")
    public ApiResponse<Map<String, Object>> socialUnlink(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable String provider
    ) {
        return ApiResponse.success(authService.socialUnlink(principal, provider));
    }
}
