package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.shortform

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode
import java.time.Instant

class ShortformService(
    private val repository: ShortformRepository,
) {
    fun upload(userId: Int, input: ShortformUploadInput): StubResponse {
        ensureUserExists(userId)
        if (input.title.trim().isBlank() || input.originalFilename.isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "title과 video 파일이 필요합니다.")
        }
        input.productIds.forEach {
            if (!repository.productExists(it)) {
                throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "상품을 찾을 수 없습니다.")
            }
        }
        val baseName = input.originalFilename.substringBeforeLast('.').ifBlank { "shortform-${Instant.now().epochSecond}" }
        val created =
            repository.createShortform(
                userId = userId,
                newShortform =
                    NewShortform(
                        title = input.title.trim(),
                        videoUrl = "/uploads/videos/$baseName.mp4",
                        thumbnailUrl = "/uploads/thumbnails/$baseName.jpg",
                        durationSec = 30,
                        transcodeStatus = "PENDING",
                        transcodedVideoUrl = null,
                        transcodeError = null,
                        transcodedAt = null,
                        productIds = input.productIds.distinct(),
                    ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = shortformPayload(created))
    }

    fun feed(cursor: Int?, limit: Int): StubResponse {
        val result = repository.listFeed(cursor = cursor, limit = normalizeCursorLimit(limit))
        return StubResponse(
            data = result.items.map(::shortformPayload),
            meta = mapOf("nextCursor" to result.nextCursor, "limit" to normalizeCursorLimit(limit)),
        )
    }

    fun detail(shortformId: Int): StubResponse {
        val record =
            repository.incrementViewCount(shortformId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "숏폼을 찾을 수 없습니다.")
        return StubResponse(data = shortformPayload(record))
    }

    fun toggleLike(userId: Int, shortformId: Int): StubResponse {
        ensureUserExists(userId)
        requireShortform(shortformId)
        val (liked, likeCount) = repository.toggleLike(userId, shortformId)
        return StubResponse(data = mapOf("liked" to liked, "likeCount" to likeCount))
    }

    fun createComment(userId: Int, shortformId: Int, request: CreateShortformCommentRequest): StubResponse {
        ensureUserExists(userId)
        requireShortform(shortformId)
        if (request.content.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "댓글 내용을 입력해주세요.")
        }
        val created = repository.createComment(userId, shortformId, request.content.trim())
        return StubResponse(status = HttpStatusCode.Created, data = commentPayload(created))
    }

    fun comments(shortformId: Int, page: Int, limit: Int): StubResponse {
        requireShortform(shortformId)
        val normalizedPage = normalizePage(page)
        val normalizedLimit = normalizeLimit(limit)
        val result = repository.listComments(shortformId, normalizedPage, normalizedLimit)
        return StubResponse(
            data = result.items.map(::commentPayload),
            meta = pageMeta(normalizedPage, normalizedLimit, result.totalCount),
        )
    }

    fun ranking(period: String?, limit: Int): StubResponse =
        StubResponse(
            data = repository.listRanking(normalizeRankingLimit(limit)).map(::shortformPayload),
            meta = mapOf("period" to normalizePeriod(period), "limit" to normalizeRankingLimit(limit)),
        )

    fun transcodeStatus(shortformId: Int): StubResponse {
        val record = requireShortform(shortformId)
        return StubResponse(
            data =
                mapOf(
                    "status" to record.transcodeStatus,
                    "errorMessage" to record.transcodeError,
                    "transcodedAt" to record.transcodedAt?.toString(),
                ),
        )
    }

    fun retry(userId: Int, isAdmin: Boolean, shortformId: Int): StubResponse {
        val record = requireShortform(shortformId)
        ensureOwnerOrAdmin(record, userId, isAdmin)
        repository.updateTranscodeStatus(shortformId, status = "PENDING", error = null, transcodedVideoUrl = null, transcodedAt = null)
        return StubResponse(data = mapOf("message" to "트랜스코딩 재시도가 예약되었습니다.", "queued" to true))
    }

    fun delete(userId: Int, isAdmin: Boolean, shortformId: Int): StubResponse {
        val record = requireShortform(shortformId)
        ensureOwnerOrAdmin(record, userId, isAdmin)
        repository.deleteShortform(shortformId)
        return StubResponse(data = mapOf("message" to "숏폼이 삭제되었습니다."))
    }

    fun userShortforms(userId: Int, page: Int, limit: Int): StubResponse {
        ensureUserExists(userId)
        val normalizedPage = normalizePage(page)
        val normalizedLimit = normalizeLimit(limit)
        val result = repository.listUserShortforms(userId, normalizedPage, normalizedLimit)
        return StubResponse(
            data = result.items.map(::shortformPayload),
            meta = pageMeta(normalizedPage, normalizedLimit, result.totalCount),
        )
    }

    private fun requireShortform(shortformId: Int): ShortformRecord =
        repository.findShortformById(shortformId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "숏폼을 찾을 수 없습니다.")

    private fun ensureOwnerOrAdmin(record: ShortformRecord, userId: Int, isAdmin: Boolean) {
        if (!isAdmin && record.userId != userId) {
            throw PbShopException(HttpStatusCode.Forbidden, "FORBIDDEN", "권한이 없습니다.")
        }
    }

    private fun ensureUserExists(userId: Int) {
        if (!repository.userExists(userId)) {
            throw PbShopException(HttpStatusCode.NotFound, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")
        }
    }

    private fun shortformPayload(record: ShortformRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "userId" to record.userId,
            "title" to record.title,
            "videoUrl" to record.videoUrl,
            "thumbnailUrl" to record.thumbnailUrl,
            "durationSec" to record.durationSec,
            "viewCount" to record.viewCount,
            "likeCount" to record.likeCount,
            "commentCount" to record.commentCount,
            "transcodeStatus" to record.transcodeStatus,
            "transcodedVideoUrl" to record.transcodedVideoUrl,
            "transcodeError" to record.transcodeError,
            "transcodedAt" to record.transcodedAt?.toString(),
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
            "user" to mapOf("id" to record.user.id, "name" to record.user.name, "nickname" to record.user.nickname, "profileImageUrl" to record.user.profileImageUrl),
            "productIds" to record.products.map { it.id },
            "products" to record.products.map { mapOf("id" to it.id, "name" to it.name, "thumbnailUrl" to it.thumbnailUrl, "lowestPrice" to it.lowestPrice) },
        )

    private fun commentPayload(record: ShortformCommentRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "shortformId" to record.shortformId,
            "userId" to record.userId,
            "content" to record.content,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
            "user" to mapOf("id" to record.user.id, "name" to record.user.name, "nickname" to record.user.nickname, "profileImageUrl" to record.user.profileImageUrl),
        )

    private fun pageMeta(page: Int, limit: Int, totalCount: Int): Map<String, Int> =
        mapOf("page" to page, "limit" to limit, "totalCount" to totalCount, "totalPages" to if (totalCount == 0) 0 else ((totalCount - 1) / limit) + 1)

    private fun normalizePage(page: Int): Int = if (page > 0) page else 1

    private fun normalizeLimit(limit: Int): Int = limit.coerceIn(1, 100).takeIf { it > 0 } ?: 20

    private fun normalizeCursorLimit(limit: Int): Int = limit.coerceIn(1, 50).takeIf { it > 0 } ?: 20

    private fun normalizeRankingLimit(limit: Int): Int = limit.coerceIn(1, 50).takeIf { it > 0 } ?: 10

    private fun normalizePeriod(period: String?): String =
        period?.trim()?.lowercase()?.takeIf { it in setOf("day", "week", "month") } ?: "day"
}
