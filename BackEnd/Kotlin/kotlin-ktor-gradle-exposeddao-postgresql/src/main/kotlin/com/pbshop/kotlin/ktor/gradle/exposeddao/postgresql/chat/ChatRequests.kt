package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatRoomCreateRequest(
    val name: String,
    val isPrivate: Boolean = true,
)

@Serializable
data class ChatSocketJoinRoomRequest(
    val roomId: Int,
)

@Serializable
data class ChatSocketSendMessageRequest(
    val roomId: Int,
    val content: String,
)
