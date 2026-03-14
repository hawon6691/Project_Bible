package com.pbshop.springshop.media;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.media.dto.MediaDtos.PresignedUrlRequest;
import com.pbshop.springshop.media.dto.MediaDtos.UploadMediaRequest;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@Service
@Transactional
public class MediaService {
    private final MediaAssetRepository mediaAssetRepository;
    private final UserRepository userRepository;
    public MediaService(MediaAssetRepository mediaAssetRepository, UserRepository userRepository) {
        this.mediaAssetRepository = mediaAssetRepository;
        this.userRepository = userRepository;
    }
    public List<Map<String, Object>> upload(AuthenticatedUserPrincipal principal, UploadMediaRequest request) {
        User user = requireCurrentUser(principal);
        return request.files().stream().map(file -> {
            MediaAsset asset = new MediaAsset();
            asset.setUser(user);
            asset.setOwnerType(request.ownerType() == null ? "SYSTEM" : request.ownerType());
            asset.setOwnerId(request.ownerId());
            asset.setFileName(file.fileName());
            asset.setFileUrl(file.fileUrl());
            asset.setFilePath(file.fileUrl().replace("https://cdn.pbshop.local/", ""));
            asset.setMimeType(file.mimeType());
            asset.setSize(file.size());
            return toResponse(mediaAssetRepository.save(asset));
        }).toList();
    }
    public Map<String, Object> presignedUrl(PresignedUrlRequest request) {
        String key = "uploads/" + UUID.randomUUID().toString().replace("-", "").substring(0, 16) + "-" + request.fileName();
        return Map.of("uploadUrl", "https://example.local/storage/" + key, "fileKey", key);
    }
    @Transactional(readOnly = true)
    public Map<String, Object> stream(Long id) {
        MediaAsset asset = requireAsset(id);
        return Map.of("id", asset.getId(), "fileUrl", asset.getFileUrl(), "mimeType", asset.getMimeType(), "size", asset.getSize(),
                "supportsRange", asset.getMimeType().startsWith("video/") || asset.getMimeType().startsWith("audio/"));
    }
    @Transactional(readOnly = true)
    public Map<String, Object> metadata(Long id) {
        MediaAsset asset = requireAsset(id);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("mime", asset.getMimeType());
        response.put("size", asset.getSize());
        response.put("duration", null);
        response.put("resolution", null);
        return response;
    }
    public Map<String, Object> delete(AuthenticatedUserPrincipal principal, Long id) {
        User user = requireCurrentUser(principal);
        MediaAsset asset = mediaAssetRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "미디어를 찾을 수 없습니다."));
        mediaAssetRepository.delete(asset);
        return Map.of("message", "미디어가 삭제되었습니다.");
    }
    private MediaAsset requireAsset(Long id) { return mediaAssetRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "미디어를 찾을 수 없습니다.")); }
    private User requireCurrentUser(AuthenticatedUserPrincipal principal) {
        if (principal == null) { throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."); }
        return userRepository.findById(principal.userId()).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
    private Map<String, Object> toResponse(MediaAsset asset) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", asset.getId());
        response.put("ownerType", asset.getOwnerType());
        response.put("ownerId", asset.getOwnerId());
        response.put("fileName", asset.getFileName());
        response.put("fileUrl", asset.getFileUrl());
        response.put("mimeType", asset.getMimeType());
        response.put("size", asset.getSize());
        response.put("createdAt", asset.getCreatedAt() == null ? null : asset.getCreatedAt().toString());
        return response;
    }
}
