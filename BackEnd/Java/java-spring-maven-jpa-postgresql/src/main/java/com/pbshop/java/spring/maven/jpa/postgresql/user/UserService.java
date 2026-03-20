package com.pbshop.java.spring.maven.jpa.postgresql.user;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.user.dto.UserDtos.UpdateMeRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.user.dto.UserDtos.UpdateProfileRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.user.dto.UserDtos.UpdateUserStatusRequest;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMe(AuthenticatedUserPrincipal principal) {
        return toUserResponse(requireCurrentUser(principal));
    }

    public Map<String, Object> updateMe(AuthenticatedUserPrincipal principal, UpdateMeRequest request) {
        User user = requireCurrentUser(principal);

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.phone() != null && !request.phone().isBlank()) {
            user.setPhone(request.phone());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        return toUserResponse(userRepository.save(user));
    }

    public Map<String, Object> deleteMe(AuthenticatedUserPrincipal principal) {
        User user = requireCurrentUser(principal);
        user.setStatus("DELETED");
        userRepository.save(user);

        return Map.of("message", "회원 탈퇴가 완료되었습니다.");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProfile(Long userId) {
        return toProfileResponse(findUser(userId));
    }

    public Map<String, Object> updateProfile(AuthenticatedUserPrincipal principal, UpdateProfileRequest request) {
        User user = requireCurrentUser(principal);

        if (request.nickname() != null && !request.nickname().isBlank()) {
            user.setName(request.nickname());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }

        return toProfileResponse(userRepository.save(user));
    }

    public Map<String, Object> updateProfileImage(AuthenticatedUserPrincipal principal, MultipartFile file) {
        User user = requireCurrentUser(principal);
        String originalFilename = file == null || file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
                ? "profile.png"
                : file.getOriginalFilename();
        user.setProfileImageUrl("https://cdn.pbshop.local/profile/" + user.getId() + "/" + originalFilename);
        userRepository.save(user);

        return Map.of("imageUrl", user.getProfileImageUrl());
    }

    public Map<String, Object> deleteProfileImage(AuthenticatedUserPrincipal principal) {
        User user = requireCurrentUser(principal);
        user.setProfileImageUrl(null);
        userRepository.save(user);

        return Map.of("message", "프로필 이미지가 삭제되었습니다.");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUsers(AuthenticatedUserPrincipal principal, int page, int limit, String search) {
        requireAdmin(principal);

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(limit, 1), Sort.by(Sort.Direction.DESC, "id"));
        Page<User> result = (search == null || search.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(search, search, pageable);

        return Map.of(
                "items", result.getContent().stream().map(this::toUserResponse).toList(),
                "pagination", Map.of(
                        "page", page,
                        "limit", limit,
                        "total", result.getTotalElements(),
                        "totalPages", result.getTotalPages()
                )
        );
    }

    public Map<String, Object> updateUserStatus(AuthenticatedUserPrincipal principal, Long userId, UpdateUserStatusRequest request) {
        requireAdmin(principal);
        User user = findUser(userId);
        user.setStatus(request.status());
        return toUserResponse(userRepository.save(user));
    }

    private User requireCurrentUser(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }

        return findUser(principal.userId());
    }

    private void requireAdmin(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private Map<String, Object> toUserResponse(User user) {
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "phone", user.getPhone() == null ? "" : user.getPhone(),
                "role", user.getRole(),
                "status", user.getStatus(),
                "emailVerified", user.isEmailVerified(),
                "profileImageUrl", user.getProfileImageUrl() == null ? "" : user.getProfileImageUrl(),
                "bio", user.getBio() == null ? "" : user.getBio()
        );
    }

    private Map<String, Object> toProfileResponse(User user) {
        return Map.of(
                "id", user.getId(),
                "nickname", user.getName(),
                "bio", user.getBio() == null ? "" : user.getBio(),
                "profileImageUrl", user.getProfileImageUrl() == null ? "" : user.getProfileImageUrl()
        );
    }
}
