package com.pbshop.springshop.badge;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.badge.dto.BadgeDtos.CreateBadgeRequest;
import com.pbshop.springshop.badge.dto.BadgeDtos.GrantBadgeRequest;
import com.pbshop.springshop.badge.dto.BadgeDtos.UpdateBadgeRequest;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@Service
@Transactional
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public BadgeService(
            BadgeRepository badgeRepository,
            UserBadgeRepository userBadgeRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getBadges() {
        return badgeRepository.findAllByOrderByIdAsc().stream()
                .map(badge -> toBadgeResponse(badge, userBadgeRepository.countByBadgeId(badge.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMyBadges(AuthenticatedUserPrincipal principal) {
        User user = requireCurrentUser(principal);
        return getUserBadges(user.getId());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserBadges(Long userId) {
        requireUser(userId);
        return userBadgeRepository.findByUserIdOrderByGrantedAtDescIdDesc(userId).stream()
                .map(this::toUserBadgeResponse)
                .toList();
    }

    public Map<String, Object> createBadge(AuthenticatedUserPrincipal principal, CreateBadgeRequest request) {
        requireAdmin(principal);
        Badge badge = new Badge();
        badge.setName(request.name());
        badge.setDescription(request.description());
        badge.setIconUrl(request.iconUrl());
        badge.setType(normalizeOrDefault(request.type(), "AUTO"));
        badge.setConditionJson(writeJson(request.condition()));
        badge.setRarity(normalizeOrDefault(request.rarity(), "COMMON"));
        return toBadgeResponse(badgeRepository.save(badge), 0);
    }

    public Map<String, Object> updateBadge(AuthenticatedUserPrincipal principal, Long badgeId, UpdateBadgeRequest request) {
        requireAdmin(principal);
        Badge badge = findBadge(badgeId);
        if (request.name() != null && !request.name().isBlank()) {
            badge.setName(request.name());
        }
        badge.setDescription(request.description());
        badge.setIconUrl(request.iconUrl());
        if (request.type() != null && !request.type().isBlank()) {
            badge.setType(normalizeOrDefault(request.type(), badge.getType()));
        }
        if (request.condition() != null) {
            badge.setConditionJson(writeJson(request.condition()));
        }
        if (request.rarity() != null && !request.rarity().isBlank()) {
            badge.setRarity(normalizeOrDefault(request.rarity(), badge.getRarity()));
        }
        return toBadgeResponse(badgeRepository.save(badge), userBadgeRepository.countByBadgeId(badge.getId()));
    }

    public Map<String, Object> deleteBadge(AuthenticatedUserPrincipal principal, Long badgeId) {
        requireAdmin(principal);
        Badge badge = findBadge(badgeId);
        badgeRepository.delete(badge);
        return Map.of("message", "배지가 삭제되었습니다.");
    }

    public Map<String, Object> grantBadge(AuthenticatedUserPrincipal principal, Long badgeId, GrantBadgeRequest request) {
        User actor = requireAdminUser(principal);
        Badge badge = findBadge(badgeId);
        User user = requireUser(request.userId());
        UserBadge userBadge = userBadgeRepository.findByBadgeIdAndUserId(badgeId, user.getId()).orElseGet(UserBadge::new);
        userBadge.setBadge(badge);
        userBadge.setUser(user);
        userBadge.setGrantedBy(actor);
        if (userBadge.getGrantedAt() == null) {
            userBadge.setGrantedAt(OffsetDateTime.now());
        }
        return toGrantResponse(userBadgeRepository.save(userBadge));
    }

    public Map<String, Object> revokeBadge(AuthenticatedUserPrincipal principal, Long badgeId, Long userId) {
        requireAdmin(principal);
        UserBadge userBadge = userBadgeRepository.findByBadgeIdAndUserId(badgeId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "부여된 배지를 찾을 수 없습니다."));
        userBadgeRepository.delete(userBadge);
        return Map.of("message", "배지가 회수되었습니다.");
    }

    private Badge findBadge(Long badgeId) {
        return badgeRepository.findById(badgeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "배지를 찾을 수 없습니다."));
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private User requireCurrentUser(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return requireUser(principal.userId());
    }

    private void requireAdmin(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }

    private User requireAdminUser(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        return requireUser(principal.userId());
    }

    private Map<String, Object> toBadgeResponse(Badge badge, long holderCount) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", badge.getId());
        response.put("name", badge.getName());
        response.put("description", badge.getDescription());
        response.put("iconUrl", badge.getIconUrl());
        response.put("type", badge.getType());
        response.put("condition", readJsonMap(badge.getConditionJson()));
        response.put("rarity", badge.getRarity());
        response.put("holderCount", holderCount);
        return response;
    }

    private Map<String, Object> toUserBadgeResponse(UserBadge userBadge) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", userBadge.getId());
        response.put("grantedAt", userBadge.getGrantedAt() == null ? null : userBadge.getGrantedAt().toString());
        response.put("badge", toBadgeResponse(userBadge.getBadge(), userBadgeRepository.countByBadgeId(userBadge.getBadge().getId())));
        return response;
    }

    private Map<String, Object> toGrantResponse(UserBadge userBadge) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", userBadge.getId());
        response.put("grantedAt", userBadge.getGrantedAt() == null ? null : userBadge.getGrantedAt().toString());
        response.put("badge", toBadgeResponse(userBadge.getBadge(), userBadgeRepository.countByBadgeId(userBadge.getBadge().getId())));
        response.put("userId", userBadge.getUser().getId());
        return response;
    }

    private String normalizeOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.toUpperCase(Locale.ROOT);
    }

    private String writeJson(Map<String, Object> value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "배지 조건을 저장할 수 없습니다.");
        }
    }

    private Map<String, Object> readJsonMap(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "배지 조건을 읽을 수 없습니다.");
        }
    }
}
