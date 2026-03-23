package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.friend

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod

fun friendOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Post, "/friends/request/{userId}", "Friend", "Send friend request", roles = setOf(PbRole.USER)) { call ->
            message("Friend request sent.", "userId" to call.pathParam("userId", "1"))
        },
        endpoint(HttpMethod.Patch, "/friends/request/{friendshipId}/accept", "Friend", "Accept friend request", roles = setOf(PbRole.USER)) {
            message("Friend request accepted.")
        },
        endpoint(HttpMethod.Patch, "/friends/request/{friendshipId}/reject", "Friend", "Reject friend request", roles = setOf(PbRole.USER)) {
            message("Friend request rejected.")
        },
        endpoint(HttpMethod.Get, "/friends", "Friend", "Friend list", roles = setOf(PbRole.USER)) {
            paged(listOf(mapOf("id" to 10, "name" to "PB Friend")))
        },
        endpoint(HttpMethod.Get, "/friends/requests/received", "Friend", "Received friend requests", roles = setOf(PbRole.USER)) {
            paged(listOf(mapOf("id" to 1, "requesterId" to 10)))
        },
        endpoint(HttpMethod.Get, "/friends/requests/sent", "Friend", "Sent friend requests", roles = setOf(PbRole.USER)) {
            paged(listOf(mapOf("id" to 2, "targetUserId" to 11)))
        },
        endpoint(HttpMethod.Get, "/friends/feed", "Friend", "Friend activity feed", roles = setOf(PbRole.USER)) {
            paged(listOf(mapOf("type" to "REVIEW_CREATED", "actorId" to 10)))
        },
        endpoint(HttpMethod.Post, "/friends/block/{userId}", "Friend", "Block user", roles = setOf(PbRole.USER)) {
            message("User blocked.")
        },
        endpoint(HttpMethod.Delete, "/friends/block/{userId}", "Friend", "Unblock user", roles = setOf(PbRole.USER)) {
            message("User unblocked.")
        },
        endpoint(HttpMethod.Delete, "/friends/{userId}", "Friend", "Delete friend", roles = setOf(PbRole.USER)) {
            message("Friend removed.")
        },
    )
