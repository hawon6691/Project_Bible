package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.builder

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun builderOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/pc-builds", "PC Builder", "PC build list", roles = setOf(PbRole.USER)) { paged(listOf(mapOf("id" to 1, "name" to "Gaming PC 2026", "purpose" to "GAMING"))) },
        endpoint(HttpMethod.Post, "/pc-builds", "PC Builder", "Create PC build", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "name" to "Gaming PC 2026", "totalPrice" to 0)) },
        endpoint(HttpMethod.Get, "/pc-builds/{id}", "PC Builder", "PC build detail") { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "parts" to emptyList<String>(), "totalPrice" to 350000)) },
        endpoint(HttpMethod.Patch, "/pc-builds/{id}", "PC Builder", "Update PC build", roles = setOf(PbRole.USER)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "name" to "Updated build")) },
        endpoint(HttpMethod.Delete, "/pc-builds/{id}", "PC Builder", "Delete PC build", roles = setOf(PbRole.USER)) { message("PC build deleted.") },
        endpoint(HttpMethod.Post, "/pc-builds/{id}/parts", "PC Builder", "Add PC part", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "parts" to listOf(mapOf("partType" to "CPU", "product" to productSummary(101, "AMD Ryzen 7 7800X3D")))))
        },
        endpoint(HttpMethod.Delete, "/pc-builds/{id}/parts/{partId}", "PC Builder", "Delete PC part", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "removedPartId" to call.pathParam("partId", "1")))
        },
        endpoint(HttpMethod.Get, "/pc-builds/{id}/compatibility", "PC Builder", "PC compatibility") { call ->
            StubResponse(data = mapOf("buildId" to call.pathParam("id", "1"), "status" to "WARNING", "warnings" to listOf("GPU is underpowered for the selected CPU.")))
        },
        endpoint(HttpMethod.Get, "/pc-builds/{id}/share", "PC Builder", "Create PC share link", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("shareUrl" to "https://pbshop.dev/pc-builds/shared/${call.pathParam("id", "1")}"))
        },
        endpoint(HttpMethod.Get, "/pc-builds/shared/{shareCode}", "PC Builder", "Shared PC build") { call ->
            StubResponse(data = mapOf("shareCode" to call.pathParam("shareCode", "PB-1234"), "name" to "Gaming PC 2026"))
        },
        endpoint(HttpMethod.Get, "/pc-builds/popular", "PC Builder", "Popular PC builds") { paged(listOf(mapOf("id" to 1, "name" to "Gaming PC 2026", "likes" to 120))) },
        endpoint(HttpMethod.Get, "/admin/compatibility-rules", "PC Builder", "Compatibility rule list", roles = setOf(PbRole.ADMIN)) { StubResponse(data = listOf(mapOf("id" to 1, "name" to "Socket match", "enabled" to true))) },
        endpoint(HttpMethod.Post, "/admin/compatibility-rules", "PC Builder", "Create compatibility rule", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "name" to "Power headroom")) },
        endpoint(HttpMethod.Patch, "/admin/compatibility-rules/{id}", "PC Builder", "Update compatibility rule", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "name" to "Updated compatibility rule"))
        },
        endpoint(HttpMethod.Delete, "/admin/compatibility-rules/{id}", "PC Builder", "Delete compatibility rule", roles = setOf(PbRole.ADMIN)) { message("Compatibility rule deleted.") },
        endpoint(HttpMethod.Post, "/friends/request/{userId}", "Friend", "Send friend request", roles = setOf(PbRole.USER)) { call -> message("Friend request sent.", "userId" to call.pathParam("userId", "1")) },
        endpoint(HttpMethod.Patch, "/friends/request/{friendshipId}/accept", "Friend", "Accept friend request", roles = setOf(PbRole.USER)) { message("Friend request accepted.") },
        endpoint(HttpMethod.Patch, "/friends/request/{friendshipId}/reject", "Friend", "Reject friend request", roles = setOf(PbRole.USER)) { message("Friend request rejected.") },
        endpoint(HttpMethod.Get, "/friends", "Friend", "Friend list", roles = setOf(PbRole.USER)) { paged(listOf(mapOf("id" to 10, "name" to "PB Friend"))) },
        endpoint(HttpMethod.Get, "/friends/requests/received", "Friend", "Received friend requests", roles = setOf(PbRole.USER)) { paged(listOf(mapOf("id" to 1, "requesterId" to 10))) },
        endpoint(HttpMethod.Get, "/friends/requests/sent", "Friend", "Sent friend requests", roles = setOf(PbRole.USER)) { paged(listOf(mapOf("id" to 2, "targetUserId" to 11))) },
        endpoint(HttpMethod.Get, "/friends/feed", "Friend", "Friend activity feed", roles = setOf(PbRole.USER)) { paged(listOf(mapOf("type" to "REVIEW_CREATED", "actorId" to 10))) },
        endpoint(HttpMethod.Post, "/friends/block/{userId}", "Friend", "Block user", roles = setOf(PbRole.USER)) { message("User blocked.") },
        endpoint(HttpMethod.Delete, "/friends/block/{userId}", "Friend", "Unblock user", roles = setOf(PbRole.USER)) { message("User unblocked.") },
        endpoint(HttpMethod.Delete, "/friends/{userId}", "Friend", "Delete friend", roles = setOf(PbRole.USER)) { message("Friend removed.") },
        endpoint(HttpMethod.Post, "/shortforms", "Shortform", "Upload shortform", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "status" to "PROCESSING", "title" to "PB shortform")) },
        endpoint(HttpMethod.Get, "/shortforms", "Shortform", "Shortform feed") { paged(listOf(mapOf("id" to 1, "title" to "PB shortform", "likeCount" to 120))) },
        endpoint(HttpMethod.Get, "/shortforms/{id}", "Shortform", "Shortform detail") { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "PB shortform", "viewCount" to 1520)) },
        endpoint(HttpMethod.Post, "/shortforms/{id}/like", "Shortform", "Toggle shortform like", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("liked" to true, "likeCount" to 121)) },
        endpoint(HttpMethod.Post, "/shortforms/{id}/comments", "Shortform", "Create shortform comment", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "content" to "Nice clip")) },
        endpoint(HttpMethod.Get, "/shortforms/{id}/comments", "Shortform", "Shortform comments") { paged(listOf(mapOf("id" to 1, "content" to "Awesome video"))) },
        endpoint(HttpMethod.Get, "/shortforms/ranking/list", "Shortform", "Shortform ranking") { paged(listOf(mapOf("id" to 1, "title" to "PB shortform", "rank" to 1))) },
        endpoint(HttpMethod.Get, "/shortforms/{id}/transcode-status", "Shortform", "Transcode status") { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "COMPLETED")) },
        endpoint(HttpMethod.Post, "/shortforms/{id}/transcode/retry", "Shortform", "Retry transcode", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("message" to "Retry queued.", "queued" to true)) },
        endpoint(HttpMethod.Delete, "/shortforms/{id}", "Shortform", "Delete shortform", roles = setOf(PbRole.USER)) { message("Shortform deleted.") },
        endpoint(HttpMethod.Get, "/shortforms/user/{userId}", "Shortform", "User shortforms") { paged(listOf(mapOf("id" to 1, "title" to "PB shortform", "userId" to 1))) },
        endpoint(HttpMethod.Get, "/news", "News", "News list") { paged(listOf(mapOf("id" to 1, "title" to "PB weekly hardware briefing"))) },
        endpoint(HttpMethod.Get, "/news/categories", "News", "News category list") { StubResponse(data = listOf(mapOf("id" to 1, "name" to "Hardware"))) },
        endpoint(HttpMethod.Get, "/news/{id}", "News", "News detail") { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "PB weekly hardware briefing", "relatedProducts" to listOf(productSummary(1, "PB GalaxyBook 4 Pro")))) },
        endpoint(HttpMethod.Post, "/news", "News", "Create news", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "title" to "New article")) },
        endpoint(HttpMethod.Patch, "/news/{id}", "News", "Update news", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "Updated article")) },
        endpoint(HttpMethod.Delete, "/news/{id}", "News", "Delete news", roles = setOf(PbRole.ADMIN)) { message("News deleted.") },
        endpoint(HttpMethod.Post, "/news/categories", "News", "Create news category", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "name" to "Deals")) },
        endpoint(HttpMethod.Delete, "/news/categories/{id}", "News", "Delete news category", roles = setOf(PbRole.ADMIN)) { message("News category deleted.") },
        endpoint(HttpMethod.Get, "/matching/pending", "Matching", "Pending product mappings", roles = setOf(PbRole.ADMIN)) { paged(listOf(mapOf("id" to 1, "sourceName" to "Vendor product", "status" to "PENDING"))) },
        endpoint(HttpMethod.Patch, "/matching/{id}/approve", "Matching", "Approve product mapping", roles = setOf(PbRole.ADMIN)) { message("Product mapping approved.") },
        endpoint(HttpMethod.Patch, "/matching/{id}/reject", "Matching", "Reject product mapping", roles = setOf(PbRole.ADMIN)) { message("Product mapping rejected.") },
        endpoint(HttpMethod.Post, "/matching/auto-match", "Matching", "Run auto match", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("matchedCount" to 12, "pendingCount" to 3)) },
        endpoint(HttpMethod.Get, "/matching/stats", "Matching", "Product mapping stats", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("approved" to 120, "pending" to 8, "rejected" to 5)) },
    )
