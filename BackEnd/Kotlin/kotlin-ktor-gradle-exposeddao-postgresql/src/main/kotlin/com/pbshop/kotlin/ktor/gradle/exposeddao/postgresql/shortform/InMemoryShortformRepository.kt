package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.shortform

import java.time.Instant

class InMemoryShortformRepository(
    users: List<ShortformUserRecord>,
    products: List<ShortformProductRecord>,
    shortforms: List<ShortformRecord>,
    comments: List<ShortformCommentRecord>,
    likes: Set<Pair<Int, Int>>,
) : ShortformRepository {
    private val users = users.associateBy { it.id }.toMutableMap()
    private val products = products.associateBy { it.id }.toMutableMap()
    private val shortforms = linkedMapOf<Int, ShortformRecord>()
    private val comments = linkedMapOf<Int, ShortformCommentRecord>()
    private val likes = likes.toMutableSet()
    private var shortformSequence = 1
    private var commentSequence = 1

    init {
        shortforms.forEach {
            this.shortforms[it.id] = it
            shortformSequence = maxOf(shortformSequence, it.id + 1)
        }
        comments.forEach {
            this.comments[it.id] = it
            commentSequence = maxOf(commentSequence, it.id + 1)
        }
    }

    override fun userExists(userId: Int): Boolean = users.containsKey(userId)

    override fun productExists(productId: Int): Boolean = products.containsKey(productId)

    override fun createShortform(userId: Int, newShortform: NewShortform): ShortformRecord {
        val now = Instant.now()
        val saved =
            ShortformRecord(
                id = shortformSequence++,
                userId = userId,
                title = newShortform.title,
                videoUrl = newShortform.videoUrl,
                thumbnailUrl = newShortform.thumbnailUrl,
                durationSec = newShortform.durationSec,
                viewCount = 0,
                likeCount = 0,
                commentCount = 0,
                transcodeStatus = newShortform.transcodeStatus,
                transcodedVideoUrl = newShortform.transcodedVideoUrl,
                transcodeError = newShortform.transcodeError,
                transcodedAt = newShortform.transcodedAt,
                createdAt = now,
                updatedAt = now,
                user = requireUser(userId),
                products = newShortform.productIds.mapNotNull(products::get),
            )
        shortforms[saved.id] = saved
        return saved
    }

    override fun listFeed(cursor: Int?, limit: Int): ShortformFeedResult {
        val sorted = shortforms.values.sortedByDescending { it.id }
        val filtered = cursor?.let { value -> sorted.filter { it.id < value } } ?: sorted
        val items = filtered.take(limit)
        return ShortformFeedResult(items = items, nextCursor = items.lastOrNull()?.id?.takeIf { filtered.size > limit })
    }

    override fun findShortformById(shortformId: Int): ShortformRecord? = shortforms[shortformId]

    override fun incrementViewCount(shortformId: Int): ShortformRecord? {
        val current = shortforms[shortformId] ?: return null
        val updated = current.copy(viewCount = current.viewCount + 1, updatedAt = Instant.now())
        shortforms[shortformId] = updated
        return updated
    }

    override fun toggleLike(userId: Int, shortformId: Int): Pair<Boolean, Int> {
        val key = shortformId to userId
        val liked = if (likes.contains(key)) {
            likes.remove(key)
            false
        } else {
            likes += key
            true
        }
        shortforms[shortformId]?.let { current ->
            val updated = current.copy(likeCount = likes.count { it.first == shortformId }, updatedAt = Instant.now())
            shortforms[shortformId] = updated
        }
        return liked to (shortforms[shortformId]?.likeCount ?: 0)
    }

    override fun createComment(userId: Int, shortformId: Int, content: String): ShortformCommentRecord {
        val now = Instant.now()
        val saved =
            ShortformCommentRecord(
                id = commentSequence++,
                shortformId = shortformId,
                userId = userId,
                content = content,
                createdAt = now,
                updatedAt = now,
                user = requireUser(userId),
            )
        comments[saved.id] = saved
        shortforms[shortformId]?.let { shortform ->
            shortforms[shortformId] = shortform.copy(commentCount = comments.values.count { it.shortformId == shortformId }, updatedAt = Instant.now())
        }
        return saved
    }

    override fun listComments(shortformId: Int, page: Int, limit: Int): ShortformPageResult<ShortformCommentRecord> {
        val sorted = comments.values.filter { it.shortformId == shortformId }.sortedByDescending { it.createdAt }
        val offset = (page - 1).coerceAtLeast(0) * limit
        return ShortformPageResult(sorted.drop(offset).take(limit), sorted.size)
    }

    override fun listRanking(limit: Int): List<ShortformRecord> =
        shortforms.values
            .sortedWith(compareByDescending<ShortformRecord> { it.likeCount }.thenByDescending { it.viewCount })
            .take(limit)

    override fun updateTranscodeStatus(shortformId: Int, status: String, error: String?, transcodedVideoUrl: String?, transcodedAt: Instant?): ShortformRecord {
        val current = shortforms[shortformId] ?: error("Shortform $shortformId not found")
        val updated =
            current.copy(
                transcodeStatus = status,
                transcodeError = error,
                transcodedVideoUrl = transcodedVideoUrl,
                transcodedAt = transcodedAt,
                updatedAt = Instant.now(),
            )
        shortforms[shortformId] = updated
        return updated
    }

    override fun deleteShortform(shortformId: Int): Boolean {
        val existed = shortforms.remove(shortformId) != null
        if (existed) {
            comments.entries.removeIf { it.value.shortformId == shortformId }
            likes.removeIf { it.first == shortformId }
        }
        return existed
    }

    override fun listUserShortforms(userId: Int, page: Int, limit: Int): ShortformPageResult<ShortformRecord> {
        val sorted = shortforms.values.filter { it.userId == userId }.sortedByDescending { it.createdAt }
        val offset = (page - 1).coerceAtLeast(0) * limit
        return ShortformPageResult(sorted.drop(offset).take(limit), sorted.size)
    }

    private fun requireUser(userId: Int): ShortformUserRecord = users[userId] ?: error("User $userId not found")

    companion object {
        fun seeded(): InMemoryShortformRepository {
            val now = Instant.parse("2026-03-12T09:00:00Z")
            val user1 = ShortformUserRecord(4, "홍길동", "hong01", null)
            val user2 = ShortformUserRecord(5, "김영희", "kim02", null)
            val product1 = ShortformProductRecord(1, "AMD Ryzen 7 7800X3D", "/products/cpu.jpg", 350000)
            val product2 = ShortformProductRecord(2, "RTX 4070 SUPER", "/products/gpu.jpg", 920000)
            val shortform1 =
                ShortformRecord(
                    id = 1,
                    userId = 4,
                    title = "게이밍 조립 팁",
                    videoUrl = "/uploads/videos/gaming-tip.mp4",
                    thumbnailUrl = "/uploads/thumbnails/gaming-tip.jpg",
                    durationSec = 30,
                    viewCount = 120,
                    likeCount = 12,
                    commentCount = 1,
                    transcodeStatus = "COMPLETED",
                    transcodedVideoUrl = "/uploads/videos/gaming-tip-1080.mp4",
                    transcodeError = null,
                    transcodedAt = now.minusSeconds(600),
                    createdAt = now.minusSeconds(7200),
                    updatedAt = now.minusSeconds(600),
                    user = user1,
                    products = listOf(product1, product2),
                )
            val comment =
                ShortformCommentRecord(
                    id = 1,
                    shortformId = 1,
                    userId = 5,
                    content = "좋은 팁 감사합니다.",
                    createdAt = now.minusSeconds(300),
                    updatedAt = now.minusSeconds(300),
                    user = user2,
                )
            return InMemoryShortformRepository(
                users = listOf(user1, user2),
                products = listOf(product1, product2),
                shortforms = listOf(shortform1),
                comments = listOf(comment),
                likes = setOf(1 to 4, 1 to 5),
            )
        }
    }
}
