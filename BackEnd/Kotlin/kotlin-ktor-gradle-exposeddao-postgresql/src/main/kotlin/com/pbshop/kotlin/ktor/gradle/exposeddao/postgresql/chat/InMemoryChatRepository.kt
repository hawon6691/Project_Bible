package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat

import java.time.Instant

private const val CHAT_CLOSE_MARKER = "__PBSHOP_ROOM_CLOSED__"

class InMemoryChatRepository(
    seededRooms: List<ChatRoomRecord> = emptyList(),
    seededMessages: List<ChatMessageRecord> = emptyList(),
) : ChatRepository {
    private val rooms = linkedMapOf<Int, ChatRoomRecord>()
    private val messages = linkedMapOf<Int, ChatMessageRecord>()
    private var nextRoomId = 1
    private var nextMessageId = 1

    init {
        seededRooms.forEach {
            rooms[it.id] = it
            nextRoomId = maxOf(nextRoomId, it.id + 1)
        }
        seededMessages.forEach {
            messages[it.id] = it
            nextMessageId = maxOf(nextMessageId, it.id + 1)
        }
    }

    override fun createRoom(
        createdBy: Int,
        room: NewChatRoom,
    ): ChatRoomRecord {
        val now = Instant.now()
        val members = linkedSetOf(createdBy)
        if (createdBy != 1) {
            members += 1
        }
        val created =
            ChatRoomRecord(
                id = nextRoomId++,
                name = room.name,
                createdBy = createdBy,
                isPrivate = room.isPrivate,
                status = "OPEN",
                createdAt = now,
                updatedAt = now,
                memberIds = members.toList(),
            )
        rooms[created.id] = created
        return created
    }

    override fun listRooms(
        userId: Int,
        isAdmin: Boolean,
        page: Int,
        limit: Int,
    ): ChatRoomListResult {
        val filtered =
            rooms.values
                .map { it.copy(status = roomStatus(it.id)) }
                .filter { isAdmin || it.memberIds.contains(userId) }
                .sortedByDescending { it.updatedAt }
        val offset = (page - 1) * limit
        return ChatRoomListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun findRoomById(id: Int): ChatRoomRecord? =
        rooms[id]?.copy(status = roomStatus(id))

    override fun isRoomMember(
        roomId: Int,
        userId: Int,
    ): Boolean = rooms[roomId]?.memberIds?.contains(userId) == true

    override fun listMessages(
        roomId: Int,
        page: Int,
        limit: Int,
    ): ChatMessageListResult {
        val filtered =
            messages.values
                .filter { it.roomId == roomId && it.message != CHAT_CLOSE_MARKER }
                .sortedBy { it.createdAt }
        val offset = (page - 1) * limit
        return ChatMessageListResult(filtered.drop(offset).take(limit), filtered.size)
    }

    override fun closeRoom(
        roomId: Int,
        actorUserId: Int,
    ): ChatRoomRecord {
        val current = requireNotNull(rooms[roomId]) { "Chat room $roomId not found" }
        val now = Instant.now()
        if (messages.values.none { it.roomId == roomId && it.message == CHAT_CLOSE_MARKER }) {
            messages[nextMessageId] =
                ChatMessageRecord(
                    id = nextMessageId++,
                    roomId = roomId,
                    senderId = actorUserId,
                    message = CHAT_CLOSE_MARKER,
                    createdAt = now,
                    updatedAt = now,
                )
        }
        val closed = current.copy(status = "CLOSED", updatedAt = now)
        rooms[roomId] = closed
        return closed
    }

    override fun createMessage(
        roomId: Int,
        senderId: Int,
        message: NewChatMessage,
    ): ChatMessageRecord {
        val now = Instant.now()
        val created =
            ChatMessageRecord(
                id = nextMessageId++,
                roomId = roomId,
                senderId = senderId,
                message = message.message,
                createdAt = now,
                updatedAt = now,
            )
        messages[created.id] = created
        rooms[roomId]?.let { room -> rooms[roomId] = room.copy(updatedAt = now) }
        return created
    }

    private fun roomStatus(roomId: Int): String =
        if (messages.values.any { it.roomId == roomId && it.message == CHAT_CLOSE_MARKER }) "CLOSED" else "OPEN"

    companion object {
        fun seeded(): InMemoryChatRepository {
            val now = Instant.now()
            return InMemoryChatRepository(
                seededRooms =
                    listOf(
                        ChatRoomRecord(
                            id = 1,
                            name = "배송 문의 방",
                            createdBy = 4,
                            isPrivate = true,
                            status = "OPEN",
                            createdAt = now.minusSeconds(360),
                            updatedAt = now.minusSeconds(300),
                            memberIds = listOf(4, 1),
                        ),
                    ),
                seededMessages =
                    listOf(
                        ChatMessageRecord(1, 1, 4, "배송 관련 문의드립니다.", now.minusSeconds(360), now.minusSeconds(360)),
                        ChatMessageRecord(2, 1, 1, "확인 후 안내드리겠습니다.", now.minusSeconds(300), now.minusSeconds(300)),
                    ),
            )
        }
    }
}
