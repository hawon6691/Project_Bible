package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.shortform

import java.time.Instant

data class ShortformUserRecord(
    val id: Int,
    val name: String,
    val nickname: String?,
    val profileImageUrl: String?,
)

data class ShortformProductRecord(
    val id: Int,
    val name: String,
    val thumbnailUrl: String?,
    val lowestPrice: Int,
)

data class ShortformCommentRecord(
    val id: Int,
    val shortformId: Int,
    val userId: Int,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val user: ShortformUserRecord,
)

data class ShortformRecord(
    val id: Int,
    val userId: Int,
    val title: String,
    val videoUrl: String,
    val thumbnailUrl: String?,
    val durationSec: Int,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val transcodeStatus: String,
    val transcodedVideoUrl: String?,
    val transcodeError: String?,
    val transcodedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val user: ShortformUserRecord,
    val products: List<ShortformProductRecord>,
)

data class ShortformFeedResult(
    val items: List<ShortformRecord>,
    val nextCursor: Int?,
)

data class ShortformPageResult<T>(
    val items: List<T>,
    val totalCount: Int,
)

data class NewShortform(
    val title: String,
    val videoUrl: String,
    val thumbnailUrl: String?,
    val durationSec: Int,
    val transcodeStatus: String,
    val transcodedVideoUrl: String?,
    val transcodeError: String?,
    val transcodedAt: Instant?,
    val productIds: List<Int>,
)

interface ShortformRepository {
    fun userExists(userId: Int): Boolean

    fun productExists(productId: Int): Boolean

    fun createShortform(userId: Int, newShortform: NewShortform): ShortformRecord

    fun listFeed(cursor: Int?, limit: Int): ShortformFeedResult

    fun findShortformById(shortformId: Int): ShortformRecord?

    fun incrementViewCount(shortformId: Int): ShortformRecord?

    fun toggleLike(userId: Int, shortformId: Int): Pair<Boolean, Int>

    fun createComment(userId: Int, shortformId: Int, content: String): ShortformCommentRecord

    fun listComments(shortformId: Int, page: Int, limit: Int): ShortformPageResult<ShortformCommentRecord>

    fun listRanking(limit: Int): List<ShortformRecord>

    fun updateTranscodeStatus(
        shortformId: Int,
        status: String,
        error: String? = null,
        transcodedVideoUrl: String? = null,
        transcodedAt: Instant? = null,
    ): ShortformRecord

    fun deleteShortform(shortformId: Int): Boolean

    fun listUserShortforms(userId: Int, page: Int, limit: Int): ShortformPageResult<ShortformRecord>
}
