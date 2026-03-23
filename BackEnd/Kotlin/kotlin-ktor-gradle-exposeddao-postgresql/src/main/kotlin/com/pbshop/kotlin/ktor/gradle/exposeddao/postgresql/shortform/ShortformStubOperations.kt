package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.shortform

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun shortformOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Post, "/shortforms", "Shortform", "Upload shortform", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "status" to "PROCESSING", "title" to "PB shortform"))
        },
        endpoint(HttpMethod.Get, "/shortforms", "Shortform", "Shortform feed") {
            paged(listOf(mapOf("id" to 1, "title" to "PB shortform", "likeCount" to 120)))
        },
        endpoint(HttpMethod.Get, "/shortforms/{id}", "Shortform", "Shortform detail") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "PB shortform", "viewCount" to 1520))
        },
        endpoint(HttpMethod.Post, "/shortforms/{id}/like", "Shortform", "Toggle shortform like", roles = setOf(PbRole.USER)) {
            StubResponse(data = mapOf("liked" to true, "likeCount" to 121))
        },
        endpoint(HttpMethod.Post, "/shortforms/{id}/comments", "Shortform", "Create shortform comment", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "content" to "Nice clip"))
        },
        endpoint(HttpMethod.Get, "/shortforms/{id}/comments", "Shortform", "Shortform comments") {
            paged(listOf(mapOf("id" to 1, "content" to "Awesome video")))
        },
        endpoint(HttpMethod.Get, "/shortforms/ranking/list", "Shortform", "Shortform ranking") {
            paged(listOf(mapOf("id" to 1, "title" to "PB shortform", "rank" to 1)))
        },
        endpoint(HttpMethod.Get, "/shortforms/{id}/transcode-status", "Shortform", "Transcode status") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "COMPLETED"))
        },
        endpoint(HttpMethod.Post, "/shortforms/{id}/transcode/retry", "Shortform", "Retry transcode", roles = setOf(PbRole.USER)) {
            StubResponse(data = mapOf("message" to "Retry queued.", "queued" to true))
        },
        endpoint(HttpMethod.Delete, "/shortforms/{id}", "Shortform", "Delete shortform", roles = setOf(PbRole.USER)) {
            message("Shortform deleted.")
        },
        endpoint(HttpMethod.Get, "/shortforms/user/{userId}", "Shortform", "User shortforms") {
            paged(listOf(mapOf("id" to 1, "title" to "PB shortform", "userId" to 1)))
        },
    )
