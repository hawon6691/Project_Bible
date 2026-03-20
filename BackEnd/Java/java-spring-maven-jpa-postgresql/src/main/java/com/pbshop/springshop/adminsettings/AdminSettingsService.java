package com.pbshop.springshop.adminsettings;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbshop.springshop.adminsettings.dto.AdminSettingsDtos.UpdateExtensionsRequest;
import com.pbshop.springshop.adminsettings.dto.AdminSettingsDtos.UpdateReviewPolicyRequest;
import com.pbshop.springshop.adminsettings.dto.AdminSettingsDtos.UpdateUploadLimitsRequest;
import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.system.SystemSetting;
import com.pbshop.springshop.system.SystemSettingRepository;

@Service
@Transactional
public class AdminSettingsService {

    private final SystemSettingRepository systemSettingRepository;
    private final ObjectMapper objectMapper;

    public AdminSettingsService(SystemSettingRepository systemSettingRepository, ObjectMapper objectMapper) {
        this.systemSettingRepository = systemSettingRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> extensions(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        return Map.of("extensions", readList("admin", "extensions", List.of("jpg", "png", "mp4", "mp3")));
    }

    public Map<String, Object> updateExtensions(AuthenticatedUserPrincipal principal, UpdateExtensionsRequest request) {
        requireAdmin(principal);
        write("admin", "extensions", request.extensions());
        return Map.of("extensions", request.extensions());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> uploadLimits(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        return readMap("admin", "upload_limits", Map.of("image", 5, "video", 100, "audio", 20));
    }

    public Map<String, Object> updateUploadLimits(AuthenticatedUserPrincipal principal, UpdateUploadLimitsRequest request) {
        requireAdmin(principal);
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("image", request.image());
        value.put("video", request.video());
        value.put("audio", request.audio());
        write("admin", "upload_limits", value);
        return value;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> reviewPolicy(AuthenticatedUserPrincipal principal) {
        requireAdmin(principal);
        return readMap("admin", "review_policy", Map.of("maxImageCount", 10, "pointAmount", 500));
    }

    public Map<String, Object> updateReviewPolicy(AuthenticatedUserPrincipal principal, UpdateReviewPolicyRequest request) {
        requireAdmin(principal);
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("maxImageCount", request.maxImageCount());
        value.put("pointAmount", request.pointAmount());
        write("admin", "review_policy", value);
        return value;
    }

    private void write(String group, String key, Object value) {
        SystemSetting setting = systemSettingRepository.findBySettingGroupAndSettingKey(group, key).orElseGet(SystemSetting::new);
        setting.setSettingGroup(group);
        setting.setSettingKey(key);
        try {
            setting.setSettingValue(objectMapper.writeValueAsString(value));
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "시스템 설정 저장에 실패했습니다.");
        }
        systemSettingRepository.save(setting);
    }

    private List<String> readList(String group, String key, List<String> defaults) {
        try {
            return systemSettingRepository.findBySettingGroupAndSettingKey(group, key)
                    .map(SystemSetting::getSettingValue)
                    .map(value -> {
                        try {
                            return objectMapper.readValue(value, new TypeReference<List<String>>() { });
                        } catch (Exception exception) {
                            return defaults;
                        }
                    })
                    .orElse(defaults);
        } catch (Exception exception) {
            return defaults;
        }
    }

    private Map<String, Object> readMap(String group, String key, Map<String, Object> defaults) {
        try {
            return systemSettingRepository.findBySettingGroupAndSettingKey(group, key)
                    .map(SystemSetting::getSettingValue)
                    .map(value -> {
                        try {
                            return objectMapper.readValue(value, new TypeReference<Map<String, Object>>() { });
                        } catch (Exception exception) {
                            return defaults;
                        }
                    })
                    .orElse(defaults);
        } catch (Exception exception) {
            return defaults;
        }
    }

    private void requireAdmin(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }
}
