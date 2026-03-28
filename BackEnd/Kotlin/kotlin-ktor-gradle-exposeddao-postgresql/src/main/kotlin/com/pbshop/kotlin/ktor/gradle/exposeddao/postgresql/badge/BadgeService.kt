package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.badge

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class BadgeService(
    private val repository: BadgeRepository,
) {
    fun listBadges(): StubResponse =
        StubResponse(data = repository.listBadges().map(::badgePayload))

    fun myBadges(userId: Int): StubResponse = StubResponse(data = repository.listUserBadges(userId).map(::userBadgePayload))

    fun userBadges(userId: Int): StubResponse {
        ensureUserExists(userId)
        return StubResponse(data = repository.listUserBadges(userId).map(::userBadgePayload))
    }

    fun create(request: CreateBadgeRequest): StubResponse {
        validateCreateRequest(request)
        if (repository.findBadgeByName(request.name.trim()) != null) {
            throw PbShopException(HttpStatusCode.Conflict, "VALIDATION_FAILED", "이미 존재하는 배지명입니다.")
        }
        val created =
            repository.createBadge(
                NewBadge(
                    name = request.name.trim(),
                    description = request.description.trim(),
                    iconUrl = request.iconUrl.trim(),
                    type = parseType(request.type),
                    condition = request.condition?.toRecord(),
                    rarity = parseRarity(request.rarity),
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = badgePayload(created))
    }

    fun update(
        badgeId: Int,
        request: UpdateBadgeRequest,
    ): StubResponse {
        val current = requireBadge(badgeId)
        request.name?.trim()?.takeIf { it.isNotBlank() && it != current.name }?.let {
            if (repository.findBadgeByName(it) != null) {
                throw PbShopException(HttpStatusCode.Conflict, "VALIDATION_FAILED", "이미 존재하는 배지명입니다.")
            }
        }
        val nextType = request.type?.let(::parseType) ?: current.type
        val nextCondition = request.condition?.toRecord() ?: current.condition
        validateCondition(nextType, nextCondition)
        val updated =
            repository.updateBadge(
                badgeId,
                BadgeUpdate(
                    name = request.name?.trim(),
                    description = request.description?.trim(),
                    iconUrl = request.iconUrl?.trim(),
                    type = request.type?.let(::parseType),
                    condition = request.condition?.toRecord(),
                    rarity = request.rarity?.let(::parseRarity),
                ),
            )
        return StubResponse(data = badgePayload(updated))
    }

    fun delete(badgeId: Int): StubResponse {
        if (!repository.deleteBadge(badgeId)) {
            throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "배지를 찾을 수 없습니다.")
        }
        return StubResponse(data = mapOf("message" to "배지가 삭제되었습니다."))
    }

    fun grant(
        badgeId: Int,
        request: GrantBadgeRequest,
        actorUserId: Int?,
    ): StubResponse {
        requireBadge(badgeId)
        ensureUserExists(request.userId)
        if (repository.hasUserBadge(badgeId, request.userId)) {
            throw PbShopException(HttpStatusCode.Conflict, "VALIDATION_FAILED", "이미 부여된 배지입니다.")
        }
        val granted =
            repository.grantBadge(
                badgeId = badgeId,
                userId = request.userId,
                grantedByAdminId = actorUserId,
                reason = request.reason?.trim()?.takeIf { it.isNotBlank() },
            )
        return StubResponse(status = HttpStatusCode.Created, data = userBadgePayload(granted))
    }

    fun revoke(
        badgeId: Int,
        userId: Int,
    ): StubResponse {
        if (!repository.revokeBadge(badgeId, userId)) {
            throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "부여된 배지를 찾을 수 없습니다.")
        }
        return StubResponse(data = mapOf("message" to "배지가 회수되었습니다."))
    }

    private fun requireBadge(id: Int): BadgeRecord =
        repository.findBadgeById(id)
            ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "배지를 찾을 수 없습니다.")

    private fun ensureUserExists(userId: Int) {
        if (!repository.userExists(userId)) {
            throw PbShopException(HttpStatusCode.NotFound, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")
        }
    }

    private fun validateCreateRequest(request: CreateBadgeRequest) {
        if (request.name.trim().isBlank() || request.description.trim().isBlank() || request.iconUrl.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_FAILED", "배지 생성 값이 올바르지 않습니다.")
        }
        validateCondition(parseType(request.type), request.condition?.toRecord())
    }

    private fun validateCondition(
        type: BadgeType,
        condition: BadgeCondition?,
    ) {
        if (type == BadgeType.AUTO && condition == null) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_FAILED", "AUTO 배지는 condition이 필요합니다.")
        }
        if (condition != null && (condition.metric.isBlank() || condition.threshold < 1)) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_FAILED", "배지 condition 값이 올바르지 않습니다.")
        }
    }

    private fun parseType(value: String): BadgeType =
        runCatching { BadgeType.valueOf(value.trim().uppercase()) }
            .getOrElse { throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_FAILED", "유효하지 않은 badge type입니다.") }

    private fun parseRarity(value: String): BadgeRarity =
        runCatching { BadgeRarity.valueOf(value.trim().uppercase()) }
            .getOrElse { throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_FAILED", "유효하지 않은 badge rarity입니다.") }

    private fun badgePayload(record: BadgeRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "name" to record.name,
            "description" to record.description,
            "iconUrl" to record.iconUrl,
            "type" to record.type.name,
            "condition" to record.condition?.let { mapOf("metric" to it.metric, "threshold" to it.threshold) },
            "rarity" to record.rarity.name,
            "holderCount" to record.holderCount,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun userBadgePayload(record: UserBadgeDetailRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "userId" to record.userId,
            "badgeId" to record.badgeId,
            "grantedByAdminId" to record.grantedByAdminId,
            "reason" to record.reason,
            "grantedAt" to record.grantedAt.toString(),
            "badge" to badgePayload(record.badge),
        )
}
