package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community

import java.time.Instant

data class BoardRecord(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String?,
    val sortOrder: Int,
    val isActive: Boolean,
)

data class PostSummaryRecord(
    val id: Int,
    val boardId: Int,
    val authorId: Int,
    val authorName: String,
    val title: String,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: Instant,
)

data class CommentRecord(
    val id: Int,
    val postId: Int,
    val userId: Int,
    val authorName: String,
    val parentId: Int?,
    val content: String,
    val createdAt: Instant,
)

data class PostDetailRecord(
    val id: Int,
    val boardId: Int,
    val authorId: Int,
    val authorName: String,
    val title: String,
    val content: String,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: Instant,
    val comments: List<CommentRecord>,
)

data class PostListResult(
    val items: List<PostSummaryRecord>,
    val totalCount: Int,
)

data class NewPost(
    val boardId: Int,
    val title: String,
    val content: String,
)

data class PostUpdate(
    val title: String?,
    val content: String?,
)

data class NewComment(
    val parentId: Int?,
    val content: String,
)

interface CommunityRepository {
    fun listBoards(): List<BoardRecord>

    fun boardExists(boardId: Int): Boolean

    fun listPosts(
        boardId: Int,
        page: Int,
        limit: Int,
        search: String?,
        sort: String?,
    ): PostListResult

    fun findPostDetail(postId: Int): PostDetailRecord?

    fun findPostSummary(postId: Int): PostSummaryRecord?

    fun createPost(
        userId: Int,
        newPost: NewPost,
    ): PostDetailRecord

    fun updatePost(
        postId: Int,
        update: PostUpdate,
    ): PostDetailRecord

    fun deletePost(postId: Int)

    fun toggleLike(
        userId: Int,
        postId: Int,
    ): Pair<Boolean, Int>

    fun listComments(postId: Int): List<CommentRecord>

    fun findCommentById(commentId: Int): CommentRecord?

    fun createComment(
        userId: Int,
        postId: Int,
        newComment: NewComment,
    ): CommentRecord

    fun deleteComment(commentId: Int)
}
