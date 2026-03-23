package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun inquiryOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/products/{productId}/inquiries", "Inquiry", "Product inquiries") { paged(listOf(mapOf("id" to 1, "title" to "Battery spec", "isSecret" to false))) },
        endpoint(HttpMethod.Post, "/products/{productId}/inquiries", "Inquiry", "Create inquiry", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "status" to "OPEN"))
        },
        endpoint(HttpMethod.Post, "/inquiries/{id}/answer", "Inquiry", "Answer inquiry", roles = setOf(PbRole.SELLER, PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "ANSWERED"))
        },
        endpoint(HttpMethod.Get, "/inquiries/me", "Inquiry", "My inquiries", roles = setOf(PbRole.USER)) { paged(listOf(mapOf("id" to 1, "title" to "Battery spec", "status" to "OPEN"))) },
        endpoint(HttpMethod.Delete, "/inquiries/{id}", "Inquiry", "Delete inquiry", roles = setOf(PbRole.USER)) { message("Inquiry deleted.") },
    )
