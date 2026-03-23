package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.engagement

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun engagementOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/boards", "Community", "Board list") {
            StubResponse(data = listOf(mapOf("id" to 1, "name" to "Reviews"), mapOf("id" to 2, "name" to "Deals")))
        },
        endpoint(HttpMethod.Get, "/boards/{boardId}/posts", "Community", "Board posts") { paged(listOf(mapOf("id" to 1, "title" to "GalaxyBook 4 usage review", "likeCount" to 45))) },
        endpoint(HttpMethod.Get, "/posts/{id}", "Community", "Post detail") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "GalaxyBook 4 usage review", "content" to "Detailed impressions"))
        },
        endpoint(HttpMethod.Post, "/boards/{boardId}/posts", "Community", "Create post", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 5, "title" to "New community post"))
        },
        endpoint(HttpMethod.Patch, "/posts/{id}", "Community", "Update post", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "Updated post"))
        },
        endpoint(HttpMethod.Delete, "/posts/{id}", "Community", "Delete post", roles = setOf(PbRole.USER, PbRole.ADMIN)) { message("Post deleted.") },
        endpoint(HttpMethod.Post, "/posts/{id}/like", "Community", "Toggle post like", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("liked" to true, "likeCount" to 46)) },
        endpoint(HttpMethod.Get, "/posts/{id}/comments", "Community", "Post comments") { StubResponse(data = listOf(mapOf("id" to 1, "content" to "Helpful review."))) },
        endpoint(HttpMethod.Post, "/posts/{id}/comments", "Community", "Create post comment", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "content" to "Great insight."))
        },
        endpoint(HttpMethod.Delete, "/comments/{id}", "Community", "Delete post comment", roles = setOf(PbRole.USER, PbRole.ADMIN)) { message("Comment deleted.") },
        endpoint(HttpMethod.Get, "/products/{productId}/inquiries", "Inquiry", "Product inquiries") { paged(listOf(mapOf("id" to 1, "title" to "Battery spec", "isSecret" to false))) },
        endpoint(HttpMethod.Post, "/products/{productId}/inquiries", "Inquiry", "Create inquiry", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "status" to "OPEN"))
        },
        endpoint(HttpMethod.Post, "/inquiries/{id}/answer", "Inquiry", "Answer inquiry", roles = setOf(PbRole.SELLER, PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "ANSWERED"))
        },
        endpoint(HttpMethod.Get, "/inquiries/me", "Inquiry", "My inquiries", roles = setOf(PbRole.USER)) { paged(listOf(mapOf("id" to 1, "title" to "Battery spec", "status" to "OPEN"))) },
        endpoint(HttpMethod.Delete, "/inquiries/{id}", "Inquiry", "Delete inquiry", roles = setOf(PbRole.USER)) { message("Inquiry deleted.") },
        endpoint(HttpMethod.Get, "/support/tickets", "Support", "Support tickets", roles = setOf(PbRole.USER)) { paged(listOf(mapOf("id" to 1, "ticketNumber" to "TK-20260323-001", "status" to "OPEN"))) },
        endpoint(HttpMethod.Post, "/support/tickets", "Support", "Create support ticket", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "ticketNumber" to "TK-20260323-002", "status" to "OPEN"))
        },
        endpoint(HttpMethod.Get, "/support/tickets/{id}", "Support", "Support ticket detail", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "OPEN", "replies" to emptyList<String>()))
        },
        endpoint(HttpMethod.Post, "/support/tickets/{id}/reply", "Support", "Reply support ticket", roles = setOf(PbRole.USER, PbRole.ADMIN)) { call ->
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to call.pathParam("id", "1"), "content" to "Reply created"))
        },
        endpoint(HttpMethod.Get, "/admin/support/tickets", "Support", "Admin support ticket list", roles = setOf(PbRole.ADMIN)) {
            paged(listOf(mapOf("id" to 1, "status" to "OPEN"), mapOf("id" to 2, "status" to "RESOLVED")))
        },
        endpoint(HttpMethod.Patch, "/admin/support/tickets/{id}/status", "Support", "Admin update support ticket status", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "RESOLVED"))
        },
        endpoint(HttpMethod.Get, "/faqs", "Help", "FAQ list") { StubResponse(data = listOf(mapOf("id" to 1, "question" to "How do I cancel an order?"))) },
        endpoint(HttpMethod.Post, "/faqs", "Help", "Create FAQ", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "question" to "How do I reset my password?"))
        },
        endpoint(HttpMethod.Patch, "/faqs/{id}", "Help", "Update FAQ", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "question" to "Updated FAQ")) },
        endpoint(HttpMethod.Delete, "/faqs/{id}", "Help", "Delete FAQ", roles = setOf(PbRole.ADMIN)) { message("FAQ deleted.") },
        endpoint(HttpMethod.Get, "/notices", "Help", "Notice list") { paged(listOf(mapOf("id" to 1, "title" to "Service maintenance notice"))) },
        endpoint(HttpMethod.Get, "/notices/{id}", "Help", "Notice detail") { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "Service maintenance notice", "content" to "Maintenance schedule")) },
        endpoint(HttpMethod.Post, "/notices", "Help", "Create notice", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "title" to "New notice")) },
        endpoint(HttpMethod.Patch, "/notices/{id}", "Help", "Update notice", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "title" to "Updated notice")) },
        endpoint(HttpMethod.Delete, "/notices/{id}", "Help", "Delete notice", roles = setOf(PbRole.ADMIN)) { message("Notice deleted.") },
        endpoint(HttpMethod.Get, "/activity/views", "Activity", "Recently viewed products", roles = setOf(PbRole.USER)) { paged(listOf(productSummary(1, "PB GalaxyBook 4 Pro"))) },
        endpoint(HttpMethod.Delete, "/activity/views", "Activity", "Clear view history", roles = setOf(PbRole.USER)) { message("View history cleared.") },
        endpoint(HttpMethod.Get, "/activity/searches", "Activity", "Search history", roles = setOf(PbRole.USER)) {
            StubResponse(data = listOf(mapOf("id" to 1, "keyword" to "galaxybook"), mapOf("id" to 2, "keyword" to "7800x3d")))
        },
        endpoint(HttpMethod.Delete, "/activity/searches", "Activity", "Clear search history", roles = setOf(PbRole.USER)) { message("Search history cleared.") },
        endpoint(HttpMethod.Delete, "/activity/searches/{id}", "Activity", "Delete search history entry", roles = setOf(PbRole.USER)) { message("Search history entry deleted.") },
        endpoint(HttpMethod.Post, "/chat/rooms", "Chat", "Create chat room", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "status" to "OPEN")) },
        endpoint(HttpMethod.Get, "/chat/rooms", "Chat", "Chat room list", roles = setOf(PbRole.USER, PbRole.ADMIN)) { paged(listOf(mapOf("id" to 1, "status" to "OPEN"), mapOf("id" to 2, "status" to "CLOSED"))) },
        endpoint(HttpMethod.Get, "/chat/rooms/{id}/messages", "Chat", "Chat room messages", roles = setOf(PbRole.USER, PbRole.ADMIN)) { paged(listOf(mapOf("id" to 1, "content" to "How can I help?"))) },
        endpoint(HttpMethod.Patch, "/chat/rooms/{id}/close", "Chat", "Close chat room", roles = setOf(PbRole.USER, PbRole.ADMIN)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "CLOSED")) },
        endpoint(HttpMethod.Post, "/chat/rooms/{id}/join", "Chat", "Join chat room", roles = setOf(PbRole.USER, PbRole.ADMIN)) { message("Joined chat room.") },
        endpoint(HttpMethod.Post, "/chat/rooms/{id}/messages", "Chat", "Send chat message", roles = setOf(PbRole.USER, PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 3, "content" to "Message sent")) },
    )
