package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.support

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun supportOperations(): List<StubOperation> =
    listOf(
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
    )
