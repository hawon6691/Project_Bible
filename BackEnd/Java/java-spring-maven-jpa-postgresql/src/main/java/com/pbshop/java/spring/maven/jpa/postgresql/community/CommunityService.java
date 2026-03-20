package com.pbshop.java.spring.maven.jpa.postgresql.community;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pbshop.java.spring.maven.jpa.postgresql.auth.security.AuthenticatedUserPrincipal;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.BusinessException;
import com.pbshop.java.spring.maven.jpa.postgresql.common.exception.ErrorCode;
import com.pbshop.java.spring.maven.jpa.postgresql.community.dto.CommunityDtos.SaveCommentRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.community.dto.CommunityDtos.SavePostRequest;
import com.pbshop.java.spring.maven.jpa.postgresql.user.User;
import com.pbshop.java.spring.maven.jpa.postgresql.user.UserRepository;

@Service
@Transactional(readOnly = true)
public class CommunityService {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    public CommunityService(
            BoardRepository boardRepository,
            PostRepository postRepository,
            PostCommentRepository postCommentRepository,
            PostLikeRepository postLikeRepository,
            UserRepository userRepository
    ) {
        this.boardRepository = boardRepository;
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
        this.postLikeRepository = postLikeRepository;
        this.userRepository = userRepository;
    }

    public List<Map<String, Object>> getBoards() {
        return boardRepository.findByActiveTrueOrderByIdAsc().stream()
                .map(this::toBoardResponse)
                .toList();
    }

    public Map<String, Object> getBoardPosts(Long boardId, String search, String sort, int page, int limit) {
        Board board = getBoard(boardId);
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), limit, resolveSort(sort));
        Page<Post> postPage = postRepository.findBoardPosts(boardId, search, pageable);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("board", toBoardResponse(board));
        response.put("items", postPage.getContent().stream().map(this::toPostSummary).toList());
        response.put("page", postPage.getNumber() + 1);
        response.put("limit", postPage.getSize());
        response.put("total", postPage.getTotalElements());
        response.put("totalPages", postPage.getTotalPages());
        return response;
    }

    @Transactional
    public Map<String, Object> getPost(Long postId) {
        Post post = getPostEntity(postId);
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        Map<String, Object> response = new LinkedHashMap<>(toPostSummary(post));
        response.put("content", post.getContent());
        response.put("comments", getComments(postId));
        return response;
    }

    @Transactional
    public Map<String, Object> createPost(AuthenticatedUserPrincipal principal, Long boardId, SavePostRequest request) {
        Board board = getBoard(boardId);
        User user = getUser(principal.userId());

        Post post = new Post();
        post.setBoard(board);
        post.setUser(user);
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setStatus("ACTIVE");
        return toPostSummary(postRepository.save(post));
    }

    @Transactional
    public Map<String, Object> updatePost(AuthenticatedUserPrincipal principal, Long postId, SavePostRequest request) {
        Post post = getPostEntity(postId);
        requireOwnerOrAdmin(principal, post.getUser().getId());
        post.setTitle(request.title());
        post.setContent(request.content());
        return toPostSummary(postRepository.save(post));
    }

    @Transactional
    public Map<String, Object> deletePost(AuthenticatedUserPrincipal principal, Long postId) {
        Post post = getPostEntity(postId);
        requireOwnerOrAdmin(principal, post.getUser().getId());
        postRepository.delete(post);
        return Map.of("message", "게시글이 삭제되었습니다.");
    }

    @Transactional
    public Map<String, Object> toggleLike(AuthenticatedUserPrincipal principal, Long postId) {
        Post post = getPostEntity(postId);
        return postLikeRepository.findByPostIdAndUserId(postId, principal.userId())
                .map(existing -> {
                    postLikeRepository.delete(existing);
                    post.setLikeCount((int) postLikeRepository.countByPostId(postId));
                    postRepository.save(post);
                    return Map.<String, Object>of("liked", false, "likeCount", post.getLikeCount());
                })
                .orElseGet(() -> {
                    PostLike like = new PostLike();
                    like.setPost(post);
                    like.setUser(getUser(principal.userId()));
                    postLikeRepository.save(like);
                    post.setLikeCount((int) postLikeRepository.countByPostId(postId));
                    postRepository.save(post);
                    return Map.<String, Object>of("liked", true, "likeCount", post.getLikeCount());
                });
    }

    public List<Map<String, Object>> getComments(Long postId) {
        ensurePostExists(postId);
        return postCommentRepository.findByPostIdOrderByIdAsc(postId).stream()
                .map(this::toCommentResponse)
                .toList();
    }

    @Transactional
    public Map<String, Object> createComment(AuthenticatedUserPrincipal principal, Long postId, SaveCommentRequest request) {
        Post post = getPostEntity(postId);
        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUser(getUser(principal.userId()));
        comment.setContent(request.content());
        PostComment saved = postCommentRepository.save(comment);
        post.setCommentCount((int) postCommentRepository.countByPostId(postId));
        postRepository.save(post);
        return toCommentResponse(saved);
    }

    @Transactional
    public Map<String, Object> deleteComment(AuthenticatedUserPrincipal principal, Long commentId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));
        requireOwnerOrAdmin(principal, comment.getUser().getId());
        Long postId = comment.getPost().getId();
        postCommentRepository.delete(comment);
        Post post = getPostEntity(postId);
        post.setCommentCount((int) postCommentRepository.countByPostId(postId));
        postRepository.save(post);
        return Map.of("message", "댓글이 삭제되었습니다.");
    }

    private Board getBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "게시판을 찾을 수 없습니다."));
    }

    private Post getPostEntity(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));
    }

    private void ensurePostExists(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private void requireOwnerOrAdmin(AuthenticatedUserPrincipal principal, Long ownerId) {
        if ("ADMIN".equalsIgnoreCase(principal.role())) {
            return;
        }
        if (!principal.userId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private Sort resolveSort(String sort) {
        if ("popular".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "likeCount").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        if ("most_commented".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "commentCount").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        return Sort.by(Sort.Direction.DESC, "id");
    }

    private Map<String, Object> toBoardResponse(Board board) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", board.getId());
        response.put("name", board.getName());
        response.put("slug", board.getSlug());
        response.put("description", board.getDescription());
        response.put("active", board.isActive());
        return response;
    }

    private Map<String, Object> toPostSummary(Post post) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", post.getId());
        response.put("boardId", post.getBoard().getId());
        response.put("boardName", post.getBoard().getName());
        response.put("userId", post.getUser().getId());
        response.put("userName", post.getUser().getName());
        response.put("title", post.getTitle());
        response.put("status", post.getStatus());
        response.put("viewCount", post.getViewCount());
        response.put("likeCount", post.getLikeCount());
        response.put("commentCount", post.getCommentCount());
        response.put("createdAt", post.getCreatedAt());
        return response;
    }

    private Map<String, Object> toCommentResponse(PostComment comment) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", comment.getId());
        response.put("postId", comment.getPost().getId());
        response.put("userId", comment.getUser().getId());
        response.put("userName", comment.getUser().getName());
        response.put("content", comment.getContent());
        response.put("createdAt", comment.getCreatedAt());
        return response;
    }
}
