package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class ChatService(
    private val repository: ChatRepository,
) {
    fun createRoom(
        userId: Int,
        request: ChatRoomCreateRequest,
    ): StubResponse {
        val name = request.name.trim()
        if (name.isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "name은 비어 있을 수 없습니다.")
        }
        val created = repository.createRoom(userId, NewChatRoom(name = name, isPrivate = request.isPrivate))
        return StubResponse(status = HttpStatusCode.Created, data = roomPayload(created))
    }

    fun rooms(
        userId: Int,
        isAdmin: Boolean,
        page: Int,
        limit: Int,
    ): StubResponse {
        val queryPage = page.coerceAtLeast(1)
        val queryLimit = limit.coerceIn(1, 100)
        val result = repository.listRooms(userId, isAdmin, queryPage, queryLimit)
        return StubResponse(
            data = result.items.map(::roomPayload),
            meta = pageMeta(queryPage, queryLimit, result.totalCount),
        )
    }

    fun messages(
        userId: Int,
        isAdmin: Boolean,
        roomId: Int,
        page: Int,
        limit: Int,
    ): StubResponse {
        val room =
            repository.findRoomById(roomId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다.")
        if (!isAdmin && !repository.isRoomMember(roomId, userId)) {
            throw PbShopException(HttpStatusCode.Forbidden, "CHAT_ROOM_FORBIDDEN", "해당 채팅방 메시지를 조회할 수 없습니다.")
        }
        val queryPage = page.coerceAtLeast(1)
        val queryLimit = limit.coerceIn(1, 100)
        val result = repository.listMessages(room.id, queryPage, queryLimit)
        return StubResponse(
            data = result.items.map(::messagePayload),
            meta = pageMeta(queryPage, queryLimit, result.totalCount),
        )
    }

    fun closeRoom(
        userId: Int,
        isAdmin: Boolean,
        roomId: Int,
    ): StubResponse {
        val room =
            repository.findRoomById(roomId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다.")
        if (!isAdmin && room.createdBy != userId) {
            throw PbShopException(HttpStatusCode.Forbidden, "CHAT_ROOM_FORBIDDEN", "채팅방을 종료할 수 없습니다.")
        }
        return StubResponse(data = roomPayload(repository.closeRoom(roomId, userId)))
    }

    fun joinRoom(
        userId: Int,
        isAdmin: Boolean,
        roomId: Int,
    ): ChatRoomRecord {
        val room =
            repository.findRoomById(roomId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다.")
        if (!isAdmin && !repository.isRoomMember(roomId, userId)) {
            throw PbShopException(HttpStatusCode.Forbidden, "CHAT_ROOM_FORBIDDEN", "해당 채팅방에 참여할 수 없습니다.")
        }
        return room
    }

    fun sendMessage(
        userId: Int,
        isAdmin: Boolean,
        roomId: Int,
        content: String,
    ): ChatMessageRecord {
        val trimmed = content.trim()
        if (trimmed.isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "message는 비어 있을 수 없습니다.")
        }
        val room =
            repository.findRoomById(roomId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다.")
        if (!isAdmin && !repository.isRoomMember(roomId, userId)) {
            throw PbShopException(HttpStatusCode.Forbidden, "CHAT_ROOM_FORBIDDEN", "해당 채팅방에 메시지를 보낼 수 없습니다.")
        }
        if (room.status == "CLOSED") {
            throw PbShopException(HttpStatusCode.BadRequest, "CHAT_ROOM_CLOSED", "종료된 채팅방에는 메시지를 보낼 수 없습니다.")
        }
        return repository.createMessage(roomId, userId, NewChatMessage(trimmed))
    }

    private fun roomPayload(record: ChatRoomRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "name" to record.name,
            "createdBy" to record.createdBy,
            "isPrivate" to record.isPrivate,
            "status" to record.status,
            "memberIds" to record.memberIds,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun messagePayload(record: ChatMessageRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "roomId" to record.roomId,
            "senderId" to record.senderId,
            "message" to record.message,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun pageMeta(
        page: Int,
        limit: Int,
        totalCount: Int,
    ): Map<String, Int> =
        mapOf(
            "page" to page,
            "limit" to limit,
            "totalCount" to totalCount,
            "totalPages" to if (totalCount == 0) 0 else ((totalCount + limit - 1) / limit),
        )
}
