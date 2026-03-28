package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ChatMessagesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ChatRoomMembersTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ChatRoomsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

private const val CHAT_CLOSE_MARKER = "__PBSHOP_ROOM_CLOSED__"

class ExposedDaoChatRepository(
    private val databaseFactory: DatabaseFactory,
) : ChatRepository {
    override fun createRoom(
        createdBy: Int,
        room: NewChatRoom,
    ): ChatRoomRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val roomId =
                ChatRoomsTable.insertAndGetId {
                    it[name] = room.name
                    it[this.createdBy] = EntityID(createdBy, UsersTable)
                    it[isPrivate] = room.isPrivate
                    it[createdAt] = now
                    it[updatedAt] = now
                }.value
            val memberIds = linkedSetOf(createdBy)
            val adminExists =
                UsersTable
                    .selectAll()
                    .where { UsersTable.id eq 1 }
                    .limit(1)
                    .any()
            if (createdBy != 1 && adminExists) {
                memberIds += 1
            }
            memberIds.forEach { userId ->
                ChatRoomMembersTable.insert {
                    it[this.room] = EntityID(roomId, ChatRoomsTable)
                    it[user] = EntityID(userId, UsersTable)
                    it[joinedAt] = now
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }
            requireNotNull(findRoomById(roomId))
        }

    override fun listRooms(
        userId: Int,
        isAdmin: Boolean,
        page: Int,
        limit: Int,
    ): ChatRoomListResult =
        databaseFactory.withTransaction {
            val roomRows =
                if (isAdmin) {
                    ChatRoomsTable
                        .selectAll()
                        .orderBy(ChatRoomsTable.updatedAt to SortOrder.DESC, ChatRoomsTable.id to SortOrder.DESC)
                        .toList()
                } else {
                    ChatRoomsTable
                        .join(ChatRoomMembersTable, JoinType.INNER, ChatRoomsTable.id, ChatRoomMembersTable.room)
                        .selectAll()
                        .where { ChatRoomMembersTable.user eq userId }
                        .orderBy(ChatRoomsTable.updatedAt to SortOrder.DESC, ChatRoomsTable.id to SortOrder.DESC)
                        .toList()
                }
            val roomIds = roomRows.map { it[ChatRoomsTable.id].value }.distinct()
            val safeIds = roomIds.ifEmpty { listOf(-1) }
            val memberMap =
                ChatRoomMembersTable
                    .selectAll()
                    .where { ChatRoomMembersTable.room inList safeIds }
                    .groupBy { it[ChatRoomMembersTable.room].value }
                    .mapValues { (_, rows) -> rows.map { it[ChatRoomMembersTable.user].value }.distinct().sorted() }
            val closedRoomIds =
                ChatMessagesTable
                    .selectAll()
                    .where { (ChatMessagesTable.room inList safeIds) and (ChatMessagesTable.message eq CHAT_CLOSE_MARKER) }
                    .map { it[ChatMessagesTable.room].value }
                    .toSet()
            val rooms =
                roomRows
                    .distinctBy { it[ChatRoomsTable.id].value }
                    .map {
                        ChatRoomRecord(
                            id = it[ChatRoomsTable.id].value,
                            name = it[ChatRoomsTable.name],
                            createdBy = it[ChatRoomsTable.createdBy].value,
                            isPrivate = it[ChatRoomsTable.isPrivate],
                            status = if (closedRoomIds.contains(it[ChatRoomsTable.id].value)) "CLOSED" else "OPEN",
                            createdAt = it[ChatRoomsTable.createdAt],
                            updatedAt = it[ChatRoomsTable.updatedAt],
                            memberIds = memberMap[it[ChatRoomsTable.id].value].orEmpty(),
                        )
                    }
            val offset = (page - 1) * limit
            ChatRoomListResult(rooms.drop(offset).take(limit), rooms.size)
        }

    override fun findRoomById(id: Int): ChatRoomRecord? =
        databaseFactory.withTransaction {
            val row =
                ChatRoomsTable
                    .selectAll()
                    .where { ChatRoomsTable.id eq id }
                    .limit(1)
                    .firstOrNull()
                    ?: return@withTransaction null
            val members =
                ChatRoomMembersTable
                    .selectAll()
                    .where { ChatRoomMembersTable.room eq id }
                    .map { it[ChatRoomMembersTable.user].value }
                    .distinct()
                    .sorted()
            val isClosed =
                ChatMessagesTable
                    .selectAll()
                    .where { (ChatMessagesTable.room eq id) and (ChatMessagesTable.message eq CHAT_CLOSE_MARKER) }
                    .limit(1)
                    .any()
            ChatRoomRecord(
                id = row[ChatRoomsTable.id].value,
                name = row[ChatRoomsTable.name],
                createdBy = row[ChatRoomsTable.createdBy].value,
                isPrivate = row[ChatRoomsTable.isPrivate],
                status = if (isClosed) "CLOSED" else "OPEN",
                createdAt = row[ChatRoomsTable.createdAt],
                updatedAt = row[ChatRoomsTable.updatedAt],
                memberIds = members,
            )
        }

    override fun isRoomMember(
        roomId: Int,
        userId: Int,
    ): Boolean =
        databaseFactory.withTransaction {
            ChatRoomMembersTable
                .selectAll()
                .where { (ChatRoomMembersTable.room eq roomId) and (ChatRoomMembersTable.user eq userId) }
                .limit(1)
                .any()
        }

    override fun listMessages(
        roomId: Int,
        page: Int,
        limit: Int,
    ): ChatMessageListResult =
        databaseFactory.withTransaction {
            val rows =
                ChatMessagesTable
                    .selectAll()
                    .where { (ChatMessagesTable.room eq roomId) and (ChatMessagesTable.message neq CHAT_CLOSE_MARKER) }
                    .orderBy(ChatMessagesTable.createdAt to SortOrder.ASC, ChatMessagesTable.id to SortOrder.ASC)
                    .map {
                        ChatMessageRecord(
                            id = it[ChatMessagesTable.id].value,
                            roomId = it[ChatMessagesTable.room].value,
                            senderId = it[ChatMessagesTable.sender].value,
                            message = it[ChatMessagesTable.message],
                            createdAt = it[ChatMessagesTable.createdAt],
                            updatedAt = it[ChatMessagesTable.updatedAt],
                        )
                    }
            val offset = (page - 1) * limit
            ChatMessageListResult(rows.drop(offset).take(limit), rows.size)
        }

    override fun closeRoom(
        roomId: Int,
        actorUserId: Int,
    ): ChatRoomRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val closeMarkerExists =
                ChatMessagesTable
                    .selectAll()
                    .where { (ChatMessagesTable.room eq roomId) and (ChatMessagesTable.message eq CHAT_CLOSE_MARKER) }
                    .limit(1)
                    .any()
            if (!closeMarkerExists) {
                ChatMessagesTable.insert {
                    it[room] = EntityID(roomId, ChatRoomsTable)
                    it[sender] = EntityID(actorUserId, UsersTable)
                    it[message] = CHAT_CLOSE_MARKER
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }
            ChatRoomsTable.update({ ChatRoomsTable.id eq roomId }) {
                it[updatedAt] = now
            }
            requireNotNull(findRoomById(roomId))
        }

    override fun createMessage(
        roomId: Int,
        senderId: Int,
        message: NewChatMessage,
    ): ChatMessageRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val messageId =
                ChatMessagesTable.insertAndGetId {
                    it[room] = EntityID(roomId, ChatRoomsTable)
                    it[sender] = EntityID(senderId, UsersTable)
                    it[ChatMessagesTable.message] = message.message
                    it[createdAt] = now
                    it[updatedAt] = now
                }.value
            ChatRoomsTable.update({ ChatRoomsTable.id eq roomId }) {
                it[updatedAt] = now
            }
            ChatMessageRecord(
                id = messageId,
                roomId = roomId,
                senderId = senderId,
                message = message.message,
                createdAt = now,
                updatedAt = now,
            )
        }
}
