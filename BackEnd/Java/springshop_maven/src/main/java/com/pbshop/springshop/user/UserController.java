package com.pbshop.springshop.user;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;
import com.pbshop.springshop.user.dto.UserDtos.UpdateMeRequest;
import com.pbshop.springshop.user.dto.UserDtos.UpdateProfileRequest;
import com.pbshop.springshop.user.dto.UserDtos.UpdateUserStatusRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> getMe(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(userService.getMe(principal));
    }

    @PatchMapping("/me")
    public ApiResponse<Map<String, Object>> updateMe(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody UpdateMeRequest request
    ) {
        return ApiResponse.success(userService.updateMe(principal, request));
    }

    @DeleteMapping("/me")
    public ApiResponse<Map<String, Object>> deleteMe(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return ApiResponse.success(userService.deleteMe(principal));
    }

    @GetMapping("/{id}/profile")
    public ApiResponse<Map<String, Object>> getProfile(@PathVariable Long id) {
        return ApiResponse.success(userService.getProfile(id));
    }

    @PatchMapping("/me/profile")
    public ApiResponse<Map<String, Object>> updateProfile(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ApiResponse.success(userService.updateProfile(principal, request));
    }

    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> uploadProfileImage(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.success(userService.updateProfileImage(principal, file));
    }

    @DeleteMapping("/me/profile-image")
    public ApiResponse<Map<String, Object>> deleteProfileImage(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return ApiResponse.success(userService.deleteProfileImage(principal));
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getUsers(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(userService.getUsers(principal, page, limit, search));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Map<String, Object>> updateUserStatus(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ApiResponse.success(userService.updateUserStatus(principal, id, request));
    }
}
