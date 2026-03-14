package com.pbshop.springshop.community;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pbshop.springshop.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.springshop.common.api.ApiResponse;
import com.pbshop.springshop.community.dto.CommunityDtos.SaveCommentRequest;
import com.pbshop.springshop.community.dto.CommunityDtos.SavePostRequest;

@RestController
@RequestMapping("${pbshop.api.base-path:/api/v1}")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @GetMapping("/boards")
    public ApiResponse<List<Map<String, Object>>> getBoards() {
        return ApiResponse.success(communityService.getBoards());
    }

    @GetMapping("/boards/{boardId}/posts")
    public ApiResponse<Map<String, Object>> getBoardPosts(
            @PathVariable Long boardId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ApiResponse.success(communityService.getBoardPosts(boardId, search, sort, page, limit));
    }

    @GetMapping("/posts/{id}")
    public ApiResponse<Map<String, Object>> getPost(@PathVariable Long id) {
        return ApiResponse.success(communityService.getPost(id));
    }

    @PostMapping("/boards/{boardId}/posts")
    public ApiResponse<Map<String, Object>> createPost(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long boardId,
            @Valid @RequestBody SavePostRequest request
    ) {
        return ApiResponse.success(communityService.createPost(principal, boardId, request));
    }

    @PatchMapping("/posts/{id}")
    public ApiResponse<Map<String, Object>> updatePost(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody SavePostRequest request
    ) {
        return ApiResponse.success(communityService.updatePost(principal, id, request));
    }

    @DeleteMapping("/posts/{id}")
    public ApiResponse<Map<String, Object>> deletePost(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(communityService.deletePost(principal, id));
    }

    @PostMapping("/posts/{id}/like")
    public ApiResponse<Map<String, Object>> toggleLike(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(communityService.toggleLike(principal, id));
    }

    @GetMapping("/posts/{id}/comments")
    public ApiResponse<List<Map<String, Object>>> getComments(@PathVariable Long id) {
        return ApiResponse.success(communityService.getComments(id));
    }

    @PostMapping("/posts/{id}/comments")
    public ApiResponse<Map<String, Object>> createComment(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody SaveCommentRequest request
    ) {
        return ApiResponse.success(communityService.createComment(principal, id, request));
    }

    @DeleteMapping("/comments/{id}")
    public ApiResponse<Map<String, Object>> deleteComment(
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ApiResponse.success(communityService.deleteComment(principal, id));
    }
}
