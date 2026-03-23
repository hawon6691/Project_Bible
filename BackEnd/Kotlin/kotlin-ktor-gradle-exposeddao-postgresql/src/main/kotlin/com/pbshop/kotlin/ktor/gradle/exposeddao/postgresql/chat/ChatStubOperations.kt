package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun chatOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Post, "/chat/rooms", "Chat", "Create chat room", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "status" to "OPEN")) },
        endpoint(HttpMethod.Get, "/chat/rooms", "Chat", "Chat room list", roles = setOf(PbRole.USER, PbRole.ADMIN)) { paged(listOf(mapOf("id" to 1, "status" to "OPEN"), mapOf("id" to 2, "status" to "CLOSED"))) },
        endpoint(HttpMethod.Get, "/chat/rooms/{id}/messages", "Chat", "Chat room messages", roles = setOf(PbRole.USER, PbRole.ADMIN)) { paged(listOf(mapOf("id" to 1, "content" to "How can I help?"))) },
        endpoint(HttpMethod.Patch, "/chat/rooms/{id}/close", "Chat", "Close chat room", roles = setOf(PbRole.USER, PbRole.ADMIN)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "CLOSED")) },
        endpoint(HttpMethod.Post, "/chat/rooms/{id}/join", "Chat", "Join chat room", roles = setOf(PbRole.USER, PbRole.ADMIN)) { message("Joined chat room.") },
        endpoint(HttpMethod.Post, "/chat/rooms/{id}/messages", "Chat", "Send chat message", roles = setOf(PbRole.USER, PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 3, "content" to "Message sent")) },
    )
