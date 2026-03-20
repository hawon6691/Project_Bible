package com.pbshop.springshop.image;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.exception.BusinessException;
import com.pbshop.springshop.common.exception.ErrorCode;
import com.pbshop.springshop.user.User;
import com.pbshop.springshop.user.UserRepository;

@Service
@Transactional
public class ImageService {

    private final ImageAssetRepository imageAssetRepository;
    private final ImageVariantRepository imageVariantRepository;
    private final UserRepository userRepository;

    public ImageService(
            ImageAssetRepository imageAssetRepository,
            ImageVariantRepository imageVariantRepository,
            UserRepository userRepository
    ) {
        this.imageAssetRepository = imageAssetRepository;
        this.imageVariantRepository = imageVariantRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> upload(AuthenticatedUserPrincipal principal, MultipartFile file, String category) {
        User user = requireCurrentUser(principal);
        String originalName = file == null || file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
                ? "image.bin"
                : file.getOriginalFilename();
        long size = file == null ? 0 : file.getSize();
        String mimeType = file == null || file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        String basePath = "uploads/original/" + user.getId() + "/" + System.currentTimeMillis() + "-" + originalName;

        ImageAsset asset = new ImageAsset();
        asset.setUser(user);
        asset.setCategory(category);
        asset.setOriginalName(originalName);
        asset.setOriginalPath(basePath);
        asset.setOriginalUrl("https://cdn.pbshop.local/" + basePath);
        asset.setMimeType(mimeType);
        asset.setSize(size);
        asset.setProcessingStatus("COMPLETED");
        ImageAsset savedAsset = imageAssetRepository.saveAndFlush(asset);

        createVariant(savedAsset, "THUMBNAIL", 200, 200, size);
        createVariant(savedAsset, "MEDIUM", 600, 600, size);
        createVariant(savedAsset, "LARGE", 1200, 1200, size);

        return toUploadResponse(savedAsset);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getVariants(Long assetId) {
        findAsset(assetId);
        return imageVariantRepository.findByImageAssetIdOrderByIdAsc(assetId).stream()
                .map(this::toVariantResponse)
                .toList();
    }

    public Map<String, Object> remove(AuthenticatedUserPrincipal principal, Long assetId) {
        requireAdmin(principal);
        ImageAsset asset = findAsset(assetId);
        imageVariantRepository.deleteByImageAssetId(asset.getId());
        imageAssetRepository.delete(asset);
        return Map.of("message", "이미지가 삭제되었습니다.");
    }

    private void createVariant(ImageAsset asset, String type, int width, int height, long originalSize) {
        ImageVariant variant = new ImageVariant();
        variant.setImageAsset(asset);
        variant.setType(type);
        variant.setPath(asset.getOriginalPath() + "." + type.toLowerCase() + ".webp");
        variant.setUrl(asset.getOriginalUrl() + "." + type.toLowerCase() + ".webp");
        variant.setWidth(width);
        variant.setHeight(height);
        variant.setFormat("webp");
        variant.setSize(Math.max(1, originalSize / 3));
        imageVariantRepository.save(variant);
    }

    private ImageAsset findAsset(Long assetId) {
        return imageAssetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "이미지를 찾을 수 없습니다."));
    }

    private User requireCurrentUser(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return userRepository.findById(principal.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private void requireAdmin(AuthenticatedUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        if (!"ADMIN".equalsIgnoreCase(principal.role())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }

    private Map<String, Object> toUploadResponse(ImageAsset asset) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", asset.getId());
        response.put("originalUrl", asset.getOriginalUrl());
        response.put("variants", getVariants(asset.getId()));
        response.put("processingStatus", asset.getProcessingStatus());
        return response;
    }

    private Map<String, Object> toVariantResponse(ImageVariant variant) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", variant.getId());
        response.put("type", variant.getType());
        response.put("url", variant.getUrl());
        response.put("width", variant.getWidth());
        response.put("height", variant.getHeight());
        response.put("format", variant.getFormat());
        response.put("size", variant.getSize());
        return response;
    }
}
