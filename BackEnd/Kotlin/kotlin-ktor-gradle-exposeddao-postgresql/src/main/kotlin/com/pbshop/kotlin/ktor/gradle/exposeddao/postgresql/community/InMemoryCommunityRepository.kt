package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community

import java.time.Instant

class InMemoryCommunityRepository(
    seededBoards: List<BoardRecord> = emptyList(),
    seededPosts: List<PostDetailRecord> = emptyList(),
    seededComments: List<CommentRecord> = emptyList(),
    seededLikes: Set<Pair<Int, Int>> = emptySet(),
) : CommunityRepository {
    private val boards = linkedMapOf<Int, BoardRecord>()
    private val posts = linkedMapOf<Int, PostDetailRecord>()
    private val comments = linkedMapOf<Int, CommentRecord>()
    private val likes = seededLikes.toMutableSet()
    private var nextPostId = 1
    private var nextCommentId = 1

    init {
        seededBoards.forEach { boards[it.id] = it }
        seededPosts.forEach {
            posts[it.id] = it
            nextPostId = maxOf(nextPostId, it.id + 1)
        }
        seededComments.forEach {
            comments[it.id] = it
            nextCommentId = maxOf(nextCommentId, it.id + 1)
        }
    }

    override fun listBoards(): List<BoardRecord> = boards.values.sortedBy { it.sortOrder }

    override fun boardExists(boardId: Int): Boolean = boards.containsKey(boardId)

    override fun listPosts(boardId: Int, page: Int, limit: Int, search: String?, sort: String?): PostListResult {
        var filtered = posts.values.filter { it.boardId == boardId }.map(::toSummary)
        search?.takeIf { it.isNotBlank() }?.let { q ->
            filtered = filtered.filter { it.title.contains(q, true) || (posts[it.id]?.content?.contains(q, true) == true) }
        }
        filtered =
            when (sort?.lowercase()) {
                "popular" -> filtered.sortedByDescending { it.likeCount }
                "most_commented" -> filtered.sortedByDescending { it.commentCount }
                else -> filtered.sortedByDescending { it.createdAt }
            }
        val offset = (page - 1) * limit
        return PostListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun findPostDetail(postId: Int): PostDetailRecord? = posts[postId]?.copy(comments = listComments(postId))

    override fun findPostSummary(postId: Int): PostSummaryRecord? = posts[postId]?.let(::toSummary)

    override fun createPost(userId: Int, newPost: NewPost): PostDetailRecord {
        val created =
            PostDetailRecord(
                id = nextPostId++,
                boardId = newPost.boardId,
                authorId = userId,
                authorName = if (userId == 4) "홍길동" else "사용자$userId",
                title = newPost.title,
                content = newPost.content,
                viewCount = 0,
                likeCount = 0,
                commentCount = 0,
                createdAt = Instant.now(),
                comments = emptyList(),
            )
        posts[created.id] = created
        return created
    }

    override fun updatePost(postId: Int, update: PostUpdate): PostDetailRecord {
        val current = requireNotNull(posts[postId]) { "Post $postId not found" }
        val updated = current.copy(title = update.title ?: current.title, content = update.content ?: current.content)
        posts[postId] = updated
        return updated.copy(comments = listComments(postId))
    }

    override fun deletePost(postId: Int) {
        posts.remove(postId)
        comments.values.removeIf { it.postId == postId }
        likes.removeIf { it.first == postId }
    }

    override fun toggleLike(userId: Int, postId: Int): Pair<Boolean, Int> {
        val key = postId to userId
        val current = requireNotNull(posts[postId]) { "Post $postId not found" }
        val liked = if (likes.contains(key)) { likes.remove(key); false } else { likes.add(key); true }
        val likeCount = likes.count { it.first == postId }
        posts[postId] = current.copy(likeCount = likeCount)
        return liked to likeCount
    }

    override fun listComments(postId: Int): List<CommentRecord> =
        comments.values.filter { it.postId == postId }.sortedBy { it.id }

    override fun findCommentById(commentId: Int): CommentRecord? = comments[commentId]

    override fun createComment(userId: Int, postId: Int, newComment: NewComment): CommentRecord {
        val created =
            CommentRecord(
                id = nextCommentId++,
                postId = postId,
                userId = userId,
                authorName = if (userId == 4) "홍길동" else "사용자$userId",
                parentId = newComment.parentId,
                content = newComment.content,
                createdAt = Instant.now(),
            )
        comments[created.id] = created
        posts[postId]?.let { posts[postId] = it.copy(commentCount = listComments(postId).size) }
        return created
    }

    override fun deleteComment(commentId: Int) {
        val removed = comments.remove(commentId) ?: return
        posts[removed.postId]?.let { posts[removed.postId] = it.copy(commentCount = listComments(removed.postId).size) }
    }

    private fun toSummary(detail: PostDetailRecord): PostSummaryRecord =
        PostSummaryRecord(detail.id, detail.boardId, detail.authorId, detail.authorName, detail.title, detail.viewCount, detail.likeCount, detail.commentCount, detail.createdAt)

    companion object {
        fun seeded(): InMemoryCommunityRepository {
            val c1 = CommentRecord(1, 1, 5, "김영희", null, "A15 모델 추천합니다.", Instant.now().minusSeconds(3500))
            val c2 = CommentRecord(2, 1, 4, "홍길동", 1, "감사합니다! 참고할게요.", Instant.now().minusSeconds(3400))
            return InMemoryCommunityRepository(
                seededBoards =
                    listOf(
                        BoardRecord(1, "자유게시판", "free", "자유롭게 이야기하는 공간", 1, true),
                        BoardRecord(2, "구매후기", "reviews", "구매 경험 공유", 2, true),
                    ),
                seededPosts =
                    listOf(
                        PostDetailRecord(1, 1, 4, "홍길동", "노트북 추천 부탁드립니다", "예산 150만원대 노트북 추천 부탁해요.", 45, 3, 2, Instant.now().minusSeconds(7200), listOf(c1, c2)),
                        PostDetailRecord(2, 2, 5, "김영희", "미니 데스크탑 사용기", "조용하고 성능 좋아서 만족합니다.", 23, 2, 1, Instant.now().minusSeconds(5400), emptyList()),
                    ),
                seededComments = listOf(c1, c2),
                seededLikes = setOf(1 to 5),
            )
        }
    }
}
