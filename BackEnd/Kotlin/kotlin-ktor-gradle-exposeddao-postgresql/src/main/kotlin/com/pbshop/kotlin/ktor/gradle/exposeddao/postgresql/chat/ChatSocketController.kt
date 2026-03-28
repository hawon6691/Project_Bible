package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthTokenCodec
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpHeaders
import io.ktor.server.routing.Route
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ChatSocketController(
    private val service: ChatService,
    private val coordinator: ChatSocketCoordinator,
) {
    fun Route.register() {
        webSocket("/chat/ws") {
            val principal = authenticate() ?: run {
                send(Frame.Text(errorPayload("인증 정보가 필요합니다.")))
                outgoing.send(Frame.Close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "AUTH_REQUIRED")))
                return@webSocket
            }

            try {
                incoming.consumeEach { frame ->
                    if (frame !is Frame.Text) {
                        return@consumeEach
                    }
                    handleTextFrame(frame.readText(), principal, this)
                }
            } finally {
                coordinator.unregister(this)
            }
        }
    }

    private suspend fun handleTextFrame(
        raw: String,
        principal: ChatSocketPrincipal,
        session: DefaultWebSocketServerSession,
    ) {
        val payload =
            runCatching { Json.parseToJsonElement(raw).jsonObject }
                .getOrElse {
                    session.send(Frame.Text(errorPayload("웹소켓 메시지 형식이 올바르지 않습니다.")))
                    return
                }
        val event = payload["event"]?.jsonPrimitive?.content
        val data = payload["data"]?.jsonObject
        when (event) {
            "joinRoom" -> {
                val roomId = data?.get("roomId")?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                handleJoinRoom(session, principal, roomId)
            }
            "sendMessage" -> {
                val roomId = data?.get("roomId")?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                val content = data?.get("content")?.jsonPrimitive?.content.orEmpty()
                handleSendMessage(session, principal, roomId, content)
            }
            else -> session.send(Frame.Text(errorPayload("지원하지 않는 이벤트입니다.")))
        }
    }

    private suspend fun handleJoinRoom(
        session: DefaultWebSocketServerSession,
        principal: ChatSocketPrincipal,
        roomId: Int,
    ) {
        runCatching {
            service.joinRoom(principal.userId, principal.role == PbRole.ADMIN, roomId)
        }.onSuccess {
            coordinator.joinRoom(roomId, session)
            session.send(Frame.Text("""{"event":"joinedRoom","data":{"roomId":$roomId}}"""))
        }.onFailure {
            session.send(Frame.Text(errorPayload(it.message ?: "채팅방 입장에 실패했습니다.")))
        }
    }

    private suspend fun handleSendMessage(
        session: DefaultWebSocketServerSession,
        principal: ChatSocketPrincipal,
        roomId: Int,
        content: String,
    ) {
        runCatching {
            service.sendMessage(principal.userId, principal.role == PbRole.ADMIN, roomId, content)
        }.onSuccess {
            coordinator.broadcastNewMessage(it)
        }.onFailure {
            session.send(Frame.Text(errorPayload(it.message ?: "메시지 전송에 실패했습니다.")))
        }
    }

    private fun DefaultWebSocketServerSession.authenticate(): ChatSocketPrincipal? {
        val bearerToken =
            call.request.headers[HttpHeaders.Authorization]
                ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
                ?.substringAfter("Bearer ")
                ?.trim()
        val bearerClaims = bearerToken?.let(AuthTokenCodec::decodeAccessToken)
        if (bearerClaims != null) {
            return ChatSocketPrincipal(userId = bearerClaims.userId, role = bearerClaims.role)
        }

        val fallbackRole = PbRole.fromHeader(call.request.headers["X-Role"]) ?: return null
        val fallbackUserId =
            call.request.headers["X-User-Id"]?.toIntOrNull()
                ?: when (fallbackRole) {
                    PbRole.USER -> 4
                    PbRole.ADMIN -> 1
                    PbRole.SELLER -> 2
                }
        return ChatSocketPrincipal(fallbackUserId, fallbackRole)
    }

    private fun errorPayload(message: String): String =
        """{"event":"error","data":{"message":${message.toJsonString()}}}"""

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

    private data class ChatSocketPrincipal(
        val userId: Int,
        val role: PbRole,
    )
}
