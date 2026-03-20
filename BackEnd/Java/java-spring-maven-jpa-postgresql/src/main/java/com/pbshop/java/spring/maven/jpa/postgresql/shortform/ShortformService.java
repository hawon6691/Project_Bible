package com.pbshop.java.spring.maven.jpa.postgresql.shortform;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.product.Product;
import com.pbshop.java.spring.maven.jpa.postgresql.product.ProductRepository;
import com.pbshop.java.spring.maven.jpa.postgresql.shortform.dto.ShortformDtos.CommentRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.shortform.dto.ShortformDtos.CreateShortformRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.user.User;
import com.pbshop.java.spring.maven.jpa.postgresql.user.UserRepository;

@Service
@Transactional
public class ShortformService {
    private final ShortformRepository shortformRepository;
    private final ShortformProductRepository shortformProductRepository;
    private final ShortformLikeRepository shortformLikeRepository;
    private final ShortformCommentRepository shortformCommentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ShortformService(ShortformRepository shortformRepository, ShortformProductRepository shortformProductRepository,
            ShortformLikeRepository shortformLikeRepository, ShortformCommentRepository shortformCommentRepository,
            ProductRepository productRepository, UserRepository userRepository) {
        this.shortformRepository = shortformRepository;
        this.shortformProductRepository = shortformProductRepository;
        this.shortformLikeRepository = shortformLikeRepository;
        this.shortformCommentRepository = shortformCommentRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> create(AuthenticatedUserPrincipal principal, CreateShortformRequest request) {
        User user = requireCurrentUser(principal);
        Shortform shortform = new Shortform();
        shortform.setUser(user);
        shortform.setTitle(request.title());
        shortform.setVideoUrl(request.videoUrl());
        shortform.setThumbnailUrl(request.thumbnailUrl());
        Shortform saved = shortformRepository.save(shortform);
        if (request.productIds() != null) {
            for (Long productId : request.productIds()) {
                Product product = productRepository.findById(productId).orElse(null);
                if (product != null) {
                    ShortformProduct relation = new ShortformProduct();
                    relation.setShortform(saved);
                    relation.setProduct(product);
                    shortformProductRepository.save(relation);
                }
            }
        }
        return toResponse(saved);
    }
    @Transactional(readOnly = true)
    public Map<String, Object> feed() {
        List<Map<String, Object>> items = shortformRepository.findAllByOrderByIdDesc().stream().map(this::toResponse).toList();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", items);
        response.put("nextCursor", null);
        return response;
    }
    @Transactional(readOnly = true)
    public Map<String, Object> show(Long id) {
        Shortform shortform = requireShortform(id);
        shortform.setViewCount(shortform.getViewCount() + 1);
        return toResponse(shortform);
    }
    public Map<String, Object> toggleLike(AuthenticatedUserPrincipal principal, Long id) {
        User user = requireCurrentUser(principal);
        Shortform shortform = requireShortform(id);
        boolean liked;
        var like = shortformLikeRepository.findByShortformIdAndUserId(id, user.getId()).orElse(null);
        if (like != null) {
            shortformLikeRepository.delete(like);
            liked = false;
        } else {
            ShortformLike newLike = new ShortformLike();
            newLike.setShortform(shortform);
            newLike.setUser(user);
            shortformLikeRepository.save(newLike);
            liked = true;
        }
        shortform.setLikeCount((int) shortformLikeRepository.countByShortformId(id));
        return Map.of("liked", liked, "likeCount", shortform.getLikeCount());
    }
    public Map<String, Object> addComment(AuthenticatedUserPrincipal principal, Long id, CommentRequest request) {
        User user = requireCurrentUser(principal);
        Shortform shortform = requireShortform(id);
        ShortformComment comment = new ShortformComment();
        comment.setShortform(shortform);
        comment.setUser(user);
        comment.setContent(request.content());
        ShortformComment saved = shortformCommentRepository.save(comment);
        shortform.setCommentCount((int) shortformCommentRepository.countByShortformId(id));
        return toComment(saved);
    }
    @Transactional(readOnly = true)
    public Map<String, Object> comments(Long id) {
        requireShortform(id);
        List<Map<String, Object>> items = shortformCommentRepository.findByShortformIdOrderByIdAsc(id).stream().map(this::toComment).toList();
        return Map.of("items", items, "pagination", Map.of("page", 1, "limit", items.isEmpty() ? 20 : items.size(), "total", items.size(), "totalPages", items.isEmpty() ? 0 : 1));
    }
    @Transactional(readOnly = true)
    public List<Map<String, Object>> ranking(String period) {
        return shortformRepository.findAllByOrderByIdDesc().stream().map(item -> {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("id", item.getId());
            response.put("period", period);
            response.put("title", item.getTitle());
            response.put("viewCount", item.getViewCount());
            response.put("likeCount", item.getLikeCount());
            response.put("commentCount", item.getCommentCount());
            response.put("videoUrl", item.getVideoUrl());
            response.put("thumbnailUrl", item.getThumbnailUrl() == null ? "" : item.getThumbnailUrl());
            response.put("userId", item.getUser().getId());
            response.put("transcodeStatus", item.getTranscodeStatus());
            response.put("createdAt", item.getCreatedAt() == null ? null : item.getCreatedAt().toString());
            response.put("updatedAt", item.getUpdatedAt() == null ? null : item.getUpdatedAt().toString());
            return response;
        }).toList();
    }
    @Transactional(readOnly = true)
    public Map<String, Object> transcodeStatus(Long id) {
        Shortform shortform = requireShortform(id);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", shortform.getTranscodeStatus());
        response.put("errorMessage", null);
        response.put("transcodedAt", shortform.getUpdatedAt() == null ? null : shortform.getUpdatedAt().toString());
        return response;
    }
    public Map<String, Object> retry(AuthenticatedUserPrincipal principal, Long id) { Shortform shortform = requireOwnedShortform(principal, id); shortform.setTranscodeStatus("QUEUED"); return Map.of("message", "트랜스코딩 재시도가 등록되었습니다.", "queued", true); }
    public Map<String, Object> delete(AuthenticatedUserPrincipal principal, Long id) { Shortform shortform = requireOwnedShortform(principal, id); shortformProductRepository.deleteByShortformId(id); shortformRepository.delete(shortform); return Map.of("message", "숏폼이 삭제되었습니다."); }
    @Transactional(readOnly = true)
    public Map<String, Object> byUser(Long userId) { List<Map<String, Object>> items = shortformRepository.findByUserIdOrderByIdDesc(userId).stream().map(this::toResponse).toList(); return Map.of("items", items, "pagination", Map.of("page", 1, "limit", items.isEmpty() ? 20 : items.size(), "total", items.size(), "totalPages", items.isEmpty() ? 0 : 1)); }

    private Shortform requireShortform(Long id) { return shortformRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "숏폼을 찾을 수 없습니다.")); }
    private Shortform requireOwnedShortform(AuthenticatedUserPrincipal principal, Long id) { Shortform shortform = requireShortform(id); User user = requireCurrentUser(principal); if (!shortform.getUser().getId().equals(user.getId())) { throw new BusinessException(ErrorCode.NOT_FOUND, "숏폼을 찾을 수 없습니다."); } return shortform; }
    private User requireCurrentUser(AuthenticatedUserPrincipal principal) { if (principal == null) { throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."); } return userRepository.findById(principal.userId()).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다.")); }
    private Map<String, Object> toResponse(Shortform shortform) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", shortform.getId());
        response.put("userId", shortform.getUser().getId());
        response.put("title", shortform.getTitle());
        response.put("videoUrl", shortform.getVideoUrl());
        response.put("thumbnailUrl", shortform.getThumbnailUrl() == null ? "" : shortform.getThumbnailUrl());
        response.put("viewCount", shortform.getViewCount());
        response.put("likeCount", shortform.getLikeCount());
        response.put("commentCount", shortform.getCommentCount());
        response.put("transcodeStatus", shortform.getTranscodeStatus());
        response.put("createdAt", shortform.getCreatedAt() == null ? null : shortform.getCreatedAt().toString());
        response.put("updatedAt", shortform.getUpdatedAt() == null ? null : shortform.getUpdatedAt().toString());
        return response;
    }
    private Map<String, Object> toComment(ShortformComment comment) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", comment.getId());
        item.put("shortformId", comment.getShortform().getId());
        item.put("userId", comment.getUser().getId());
        item.put("content", comment.getContent());
        item.put("createdAt", comment.getCreatedAt() == null ? null : comment.getCreatedAt().toString());
        return item;
    }
}
