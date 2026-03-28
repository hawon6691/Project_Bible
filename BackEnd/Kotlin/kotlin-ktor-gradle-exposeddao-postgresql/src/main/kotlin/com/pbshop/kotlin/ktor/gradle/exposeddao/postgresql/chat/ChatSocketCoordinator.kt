package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class ChatSocketCoordinator {
    private val sessionsByRoom = ConcurrentHashMap<Int, MutableSet<DefaultWebSocketServerSession>>()
    private val roomsBySession = ConcurrentHashMap<DefaultWebSocketServerSession, MutableSet<Int>>()

    fun joinRoom(
        roomId: Int,
        session: DefaultWebSocketServerSession,
    ) {
        sessionsByRoom.computeIfAbsent(roomId) { Collections.synchronizedSet(linkedSetOf()) }.add(session)
        roomsBySession.computeIfAbsent(session) { Collections.synchronizedSet(linkedSetOf()) }.add(roomId)
    }

    suspend fun broadcastNewMessage(record: ChatMessageRecord) {
        val payload =
            """
            {"event":"newMessage","data":{"id":${record.id},"roomId":${record.roomId},"senderId":${record.senderId},"content":${record.message.toJsonString()},"createdAt":"${record.createdAt}"}} 
            """.trimIndent()
        sessionsByRoom[record.roomId]?.toList()?.forEach { session ->
            runCatching {
                session.send(Frame.Text(payload))
            }.onFailure {
                unregister(session)
            }
        }
    }

    fun unregister(session: DefaultWebSocketServerSession) {
        roomsBySession.remove(session)?.forEach { roomId ->
            sessionsByRoom[roomId]?.remove(session)
            if (sessionsByRoom[roomId].isNullOrEmpty()) {
                sessionsByRoom.remove(roomId)
            }
        }
    }

    private fun String.toJsonString(): String =
        buildString(length + 2) {
            append('"')
            this@toJsonString.forEach { ch ->
                when (ch) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(ch)
                }
            }
            append('"')
        }
}
