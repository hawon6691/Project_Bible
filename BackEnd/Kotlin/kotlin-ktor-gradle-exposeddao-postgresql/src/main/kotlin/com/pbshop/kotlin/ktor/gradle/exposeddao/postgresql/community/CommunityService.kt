package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class CommunityService(
    private val repository: CommunityRepository,
) {
    fun boards(): StubResponse =
        StubResponse(data = repository.listBoards().map(::boardPayload))

    fun posts(
        boardId: Int,
        page: Int,
        limit: Int,
        search: String?,
        sort: String?,
    ): StubResponse {
        ensureBoardExists(boardId)
        val queryPage = if (page < 1) 1 else page
        val queryLimit = limit.coerceIn(1, 100)
        val result = repository.listPosts(boardId, queryPage, queryLimit, search?.trim(), sort?.trim())
        return StubResponse(
            data = result.items.map(::postSummaryPayload),
            meta =
                mapOf(
                    "page" to queryPage,
                    "limit" to queryLimit,
                    "totalCount" to result.totalCount,
                    "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount + queryLimit - 1) / queryLimit),
                ),
        )
    }

    fun detail(postId: Int): StubResponse =
        StubResponse(data = postDetailPayload(requirePost(postId)))

    fun createPost(
        userId: Int,
        boardId: Int,
        request: PostRequest,
    ): StubResponse {
        ensureBoardExists(boardId)
        validatePost(request.title, request.content)
        val created = repository.createPost(userId, NewPost(boardId, request.title.trim(), request.content.trim()))
        return StubResponse(status = HttpStatusCode.Created, data = postDetailPayload(created))
    }

    fun updatePost(
        userId: Int,
        isAdmin: Boolean,
        postId: Int,
        request: PostUpdateRequest,
    ): StubResponse {
        if (request.title == null && request.content == null) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "수정할 값이 없습니다.")
        }
        val current = requirePost(postId)
        if (!isAdmin && current.authorId != userId) {
            throw PbShopException(HttpStatusCode.Forbidden, "POST_FORBIDDEN", "본인의 게시글만 수정할 수 있습니다.")
        }
        request.title?.let { if (it.trim().isBlank()) throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "title은 비어 있을 수 없습니다.") }
        request.content?.let { if (it.trim().isBlank()) throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "content는 비어 있을 수 없습니다.") }
        val updated = repository.updatePost(postId, PostUpdate(request.title?.trim(), request.content?.trim()))
        return StubResponse(data = postDetailPayload(updated))
    }

    fun deletePost(
        userId: Int?,
        isAdmin: Boolean,
        postId: Int,
    ): StubResponse {
        val current = requirePost(postId)
        if (!isAdmin && current.authorId != userId) {
            throw PbShopException(HttpStatusCode.Forbidden, "POST_FORBIDDEN", "본인의 게시글만 삭제할 수 있습니다.")
        }
        repository.deletePost(postId)
        return StubResponse(data = mapOf("message" to "Post deleted."))
    }

    fun toggleLike(
        userId: Int,
        postId: Int,
    ): StubResponse {
        requirePost(postId)
        val (liked, likeCount) = repository.toggleLike(userId, postId)
        return StubResponse(data = mapOf("liked" to liked, "likeCount" to likeCount))
    }

    fun comments(postId: Int): StubResponse {
        requirePost(postId)
        return StubResponse(data = repository.listComments(postId).map(::commentPayload))
    }

    fun createComment(
        userId: Int,
        postId: Int,
        request: CommentRequest,
    ): StubResponse {
        requirePost(postId)
        if (request.content.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "content는 비어 있을 수 없습니다.")
        }
        val created = repository.createComment(userId, postId, NewComment(request.parentId, request.content.trim()))
        return StubResponse(status = HttpStatusCode.Created, data = commentPayload(created))
    }

    fun deleteComment(
        userId: Int?,
        isAdmin: Boolean,
        commentId: Int,
    ): StubResponse {
        val comment =
            repository.findCommentById(commentId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "COMMENT_NOT_FOUND", "댓글을 찾을 수 없습니다.")
        if (!isAdmin && comment.userId != userId) {
            throw PbShopException(HttpStatusCode.Forbidden, "COMMENT_FORBIDDEN", "본인의 댓글만 삭제할 수 있습니다.")
        }
        repository.deleteComment(commentId)
        return StubResponse(data = mapOf("message" to "Comment deleted."))
    }

    private fun ensureBoardExists(boardId: Int) {
        if (!repository.boardExists(boardId)) {
            throw PbShopException(HttpStatusCode.NotFound, "BOARD_NOT_FOUND", "게시판을 찾을 수 없습니다.")
        }
    }

    private fun requirePost(postId: Int): PostDetailRecord =
        repository.findPostDetail(postId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "POST_NOT_FOUND", "게시글을 찾을 수 없습니다.")

    private fun validatePost(
        title: String,
        content: String,
    ) {
        if (title.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "title은 비어 있을 수 없습니다.")
        }
        if (content.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "content는 비어 있을 수 없습니다.")
        }
    }

    private fun boardPayload(record: BoardRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "name" to record.name,
            "slug" to record.slug,
            "description" to record.description,
            "sortOrder" to record.sortOrder,
            "isActive" to record.isActive,
        )

    private fun postSummaryPayload(record: PostSummaryRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "title" to record.title,
            "author" to mapOf("id" to record.authorId, "name" to record.authorName, "badges" to emptyList<String>()),
            "viewCount" to record.viewCount,
            "likeCount" to record.likeCount,
            "commentCount" to record.commentCount,
            "createdAt" to record.createdAt.toString(),
        )

    private fun commentPayload(record: CommentRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "postId" to record.postId,
            "author" to mapOf("id" to record.userId, "name" to record.authorName),
            "parentId" to record.parentId,
            "content" to record.content,
            "createdAt" to record.createdAt.toString(),
        )

    private fun postDetailPayload(record: PostDetailRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "boardId" to record.boardId,
            "title" to record.title,
            "content" to record.content,
            "author" to mapOf("id" to record.authorId, "name" to record.authorName, "badges" to emptyList<String>()),
            "viewCount" to record.viewCount,
            "likeCount" to record.likeCount,
            "commentCount" to record.commentCount,
            "comments" to record.comments.map(::commentPayload),
            "createdAt" to record.createdAt.toString(),
        )
}
