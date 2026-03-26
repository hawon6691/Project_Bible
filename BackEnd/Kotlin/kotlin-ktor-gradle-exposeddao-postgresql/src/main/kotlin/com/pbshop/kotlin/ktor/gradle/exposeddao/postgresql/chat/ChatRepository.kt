package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat

import java.time.Instant

data class ChatRoomRecord(
    val id: Int,
    val name: String,
    val createdBy: Int,
    val isPrivate: Boolean,
    val status: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val memberIds: List<Int>,
)

data class ChatRoomListResult(
    val items: List<ChatRoomRecord>,
    val totalCount: Int,
)

data class ChatMessageRecord(
    val id: Int,
    val roomId: Int,
    val senderId: Int,
    val message: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ChatMessageListResult(
    val items: List<ChatMessageRecord>,
    val totalCount: Int,
)

data class NewChatRoom(
    val name: String,
    val isPrivate: Boolean,
)

interface ChatRepository {
    fun createRoom(
        createdBy: Int,
        room: NewChatRoom,
    ): ChatRoomRecord

    fun listRooms(
        userId: Int,
        isAdmin: Boolean,
        page: Int,
        limit: Int,
    ): ChatRoomListResult

    fun findRoomById(id: Int): ChatRoomRecord?

    fun isRoomMember(
        roomId: Int,
        userId: Int,
    ): Boolean

    fun listMessages(
        roomId: Int,
        page: Int,
        limit: Int,
    ): ChatMessageListResult

    fun closeRoom(
        roomId: Int,
        actorUserId: Int,
    ): ChatRoomRecord
}
