package com.pbshop.springshop.media;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;
import com.pbshop.springshop.media.dto.MediaDtos.PresignedUrlRequest;
import com.pbshop.springshop.media.dto.MediaDtos.UploadMediaRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}/media")
public class MediaController {
    private final MediaService mediaService;
    public MediaController(MediaService mediaService) { this.mediaService = mediaService; }
    @PostMapping("/upload") public ApiResponse<List<Map<String, Object>>> upload(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @Valid @RequestBody UploadMediaRequest request) { return ApiResponse.success(mediaService.upload(principal, request)); }
    @PostMapping("/presigned-url") public ApiResponse<Map<String, Object>> presignedUrl(@Valid @RequestBody PresignedUrlRequest request) { return ApiResponse.success(mediaService.presignedUrl(request)); }
    @GetMapping("/stream/{id}") public ApiResponse<Map<String, Object>> stream(@PathVariable Long id) { return ApiResponse.success(mediaService.stream(id)); }
    @GetMapping("/{id}/metadata") public ApiResponse<Map<String, Object>> metadata(@PathVariable Long id) { return ApiResponse.success(mediaService.metadata(id)); }
    @DeleteMapping("/{id}") public ApiResponse<Map<String, Object>> delete(@AuthenticationPrincipal AuthenticatedUserPrincipal principal, @PathVariable Long id) { return ApiResponse.success(mediaService.delete(principal, id)); }
}
