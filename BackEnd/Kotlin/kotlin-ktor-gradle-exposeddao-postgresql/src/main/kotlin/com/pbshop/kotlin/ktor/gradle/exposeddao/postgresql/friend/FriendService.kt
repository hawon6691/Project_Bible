package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.friend

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class FriendService(
    private val repository: FriendRepository,
) {
    fun requestFriend(userId: Int, targetUserId: Int): StubResponse {
        ensureUser(userId)
        ensureUser(targetUserId)
        ensureNotSelf(userId, targetUserId)
        ensureNotBlocked(userId, targetUserId)
        val existing = repository.findFriendshipPair(userId, targetUserId)
        if (existing?.status == "ACCEPTED") {
            throw PbShopException(HttpStatusCode.Conflict, "VALIDATION_ERROR", "이미 친구입니다.")
        }
        if (existing?.status == "PENDING") {
            throw PbShopException(HttpStatusCode.Conflict, "VALIDATION_ERROR", "이미 친구 요청이 존재합니다.")
        }
        repository.saveFriendship(userId, targetUserId, "PENDING", existing?.id)
        return StubResponse(data = mapOf("message" to "친구 요청을 보냈습니다."))
    }

    fun acceptRequest(userId: Int, friendshipId: Int): StubResponse {
        val friendship = requireFriendship(friendshipId)
        if (friendship.addresseeId != userId) {
            throw PbShopException(HttpStatusCode.Forbidden, "FORBIDDEN", "수락 권한이 없습니다.")
        }
        if (friendship.status != "PENDING") {
            throw PbShopException(HttpStatusCode.BadRequest, "INVALID_STATUS_TRANSITION", "대기중인 요청만 수락할 수 있습니다.")
        }
        repository.saveFriendship(friendship.requesterId, friendship.addresseeId, "ACCEPTED", friendship.id)
        repository.createActivity(friendship.requesterId, "FRIEND_ACCEPTED", "유저 ${friendship.addresseeId}님과 친구가 되었습니다.", mapOf("friendUserId" to friendship.addresseeId.toString()))
        repository.createActivity(friendship.addresseeId, "FRIEND_ACCEPTED", "유저 ${friendship.requesterId}님과 친구가 되었습니다.", mapOf("friendUserId" to friendship.requesterId.toString()))
        return StubResponse(data = mapOf("message" to "친구 요청을 수락했습니다."))
    }

    fun rejectRequest(userId: Int, friendshipId: Int): StubResponse {
        val friendship = requireFriendship(friendshipId)
        if (friendship.addresseeId != userId) {
            throw PbShopException(HttpStatusCode.Forbidden, "FORBIDDEN", "거절 권한이 없습니다.")
        }
        if (friendship.status != "PENDING") {
            throw PbShopException(HttpStatusCode.BadRequest, "INVALID_STATUS_TRANSITION", "대기중인 요청만 거절할 수 있습니다.")
        }
        repository.saveFriendship(friendship.requesterId, friendship.addresseeId, "REJECTED", friendship.id)
        return StubResponse(data = mapOf("message" to "친구 요청을 거절했습니다."))
    }

    fun listFriends(userId: Int, page: Int, limit: Int): StubResponse {
        val result = repository.listFriends(userId, normalizePage(page), normalizeLimit(limit))
        return StubResponse(
            data = result.items.map { friendshipPayload(it, userId) },
            meta = pageMeta(page, limit, result.totalCount),
        )
    }

    fun receivedRequests(userId: Int, page: Int, limit: Int): StubResponse {
        val normalizedPage = normalizePage(page)
        val normalizedLimit = normalizeLimit(limit)
        val result = repository.listReceivedRequests(userId, normalizedPage, normalizedLimit)
        return StubResponse(data = result.items.map(::requestPayload), meta = pageMeta(normalizedPage, normalizedLimit, result.totalCount))
    }

    fun sentRequests(userId: Int, page: Int, limit: Int): StubResponse {
        val normalizedPage = normalizePage(page)
        val normalizedLimit = normalizeLimit(limit)
        val result = repository.listSentRequests(userId, normalizedPage, normalizedLimit)
        return StubResponse(data = result.items.map(::requestPayload), meta = pageMeta(normalizedPage, normalizedLimit, result.totalCount))
    }

    fun feed(userId: Int, page: Int, limit: Int): StubResponse {
        val normalizedPage = normalizePage(page)
        val normalizedLimit = normalizeLimit(limit)
        val result = repository.listFeed(userId, normalizedPage, normalizedLimit)
        return StubResponse(data = result.items.map(::activityPayload), meta = pageMeta(normalizedPage, normalizedLimit, result.totalCount))
    }

    fun blockUser(userId: Int, targetUserId: Int): StubResponse {
        ensureUser(userId)
        ensureUser(targetUserId)
        ensureNotSelf(userId, targetUserId)
        repository.createBlock(userId, targetUserId)
        repository.deleteFriendshipPair(userId, targetUserId)
        return StubResponse(data = mapOf("message" to "사용자를 차단했습니다."))
    }

    fun unblockUser(userId: Int, targetUserId: Int): StubResponse {
        if (!repository.deleteBlock(userId, targetUserId)) {
            throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "차단 정보를 찾을 수 없습니다.")
        }
        return StubResponse(data = mapOf("message" to "차단을 해제했습니다."))
    }

    fun removeFriend(userId: Int, targetUserId: Int): StubResponse {
        val friendship = repository.findFriendshipPair(userId, targetUserId)
        if (friendship == null || friendship.status != "ACCEPTED") {
            throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "친구 관계를 찾을 수 없습니다.")
        }
        repository.deleteFriendshipPair(userId, targetUserId)
        return StubResponse(data = mapOf("message" to "친구를 삭제했습니다."))
    }

    private fun ensureUser(userId: Int) {
        if (!repository.userExists(userId)) {
            throw PbShopException(HttpStatusCode.NotFound, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.")
        }
    }

    private fun ensureNotSelf(userId: Int, targetUserId: Int) {
        if (userId == targetUserId) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "자기 자신에게는 수행할 수 없습니다.")
        }
    }

    private fun ensureNotBlocked(userId: Int, targetUserId: Int) {
        if (repository.isBlockedBetween(userId, targetUserId)) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "차단된 사용자와는 요청할 수 없습니다.")
        }
    }

    private fun requireFriendship(friendshipId: Int): FriendshipRecord =
        repository.findFriendshipById(friendshipId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "친구 요청을 찾을 수 없습니다.")

    private fun friendshipPayload(record: FriendshipRecord, currentUserId: Int): Map<String, Any?> {
        val friend = if (record.requesterId == currentUserId) record.addressee else record.requester
        return mapOf(
            "id" to record.id,
            "status" to record.status,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
            "friend" to userPayload(friend),
        )
    }

    private fun requestPayload(record: FriendshipRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "status" to record.status,
            "createdAt" to record.createdAt.toString(),
            "requester" to userPayload(record.requester),
            "addressee" to userPayload(record.addressee),
        )

    private fun activityPayload(record: FriendActivityRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "userId" to record.userId,
            "type" to record.type,
            "message" to record.message,
            "metadata" to record.metadata,
            "createdAt" to record.createdAt.toString(),
            "user" to userPayload(record.user),
        )

    private fun userPayload(record: FriendUserRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "email" to record.email,
            "name" to record.name,
            "nickname" to record.nickname,
            "profileImageUrl" to record.profileImageUrl,
            "role" to record.role,
        )

    private fun normalizePage(page: Int): Int = if (page > 0) page else 1

    private fun normalizeLimit(limit: Int): Int = limit.coerceIn(1, 100).takeIf { it > 0 } ?: 20

    private fun pageMeta(page: Int, limit: Int, totalCount: Int): Map<String, Int> =
        mapOf(
            "page" to page,
            "limit" to limit,
            "totalCount" to totalCount,
            "totalPages" to if (totalCount == 0) 0 else ((totalCount - 1) / limit) + 1,
        )
}
