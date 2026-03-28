package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.badge

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun badgeOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/badges", "Badge", "Badge list") {
            StubResponse(data = listOf(mapOf("id" to 1, "name" to "Review Master", "description" to "리뷰 10개 이상 작성", "holderCount" to 1523)))
        },
        endpoint(HttpMethod.Get, "/badges/me", "Badge", "Current user badges", roles = setOf(PbRole.USER)) {
            StubResponse(data = listOf(mapOf("userId" to 4, "badge" to mapOf("id" to 1, "name" to "Review Master"))))
        },
        endpoint(HttpMethod.Get, "/users/{id}/badges", "Badge", "User badges") { call ->
            StubResponse(data = listOf(mapOf("userId" to call.pathParam("id", "1"), "badge" to mapOf("id" to 1, "name" to "Review Master"))))
        },
        endpoint(HttpMethod.Post, "/admin/badges", "Badge", "Create badge", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "name" to "Purchase King")) },
        endpoint(HttpMethod.Patch, "/admin/badges/{id}", "Badge", "Update badge", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "name" to "Updated badge")) },
        endpoint(HttpMethod.Delete, "/admin/badges/{id}", "Badge", "Delete badge", roles = setOf(PbRole.ADMIN)) { message("Badge deleted.") },
        endpoint(HttpMethod.Post, "/admin/badges/{id}/grant", "Badge", "Grant badge", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(status = HttpStatusCode.Created, data = mapOf("badgeId" to call.pathParam("id", "1"), "userId" to 1, "reason" to "manual grant"))
        },
        endpoint(HttpMethod.Delete, "/admin/badges/{id}/revoke/{userId}", "Badge", "Revoke badge", roles = setOf(PbRole.ADMIN)) { message("Badge revoked.") },
    )
