package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun addressOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/addresses", "Address", "Address list", roles = setOf(PbRole.USER)) {
            StubResponse(data = listOf(mapOf("id" to 1, "recipient" to "PB User", "city" to "Seoul", "isDefault" to true)))
        },
        endpoint(HttpMethod.Post, "/addresses", "Address", "Create address", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "recipient" to "PB User", "city" to "Busan"))
        },
        endpoint(HttpMethod.Patch, "/addresses/{id}", "Address", "Update address", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "recipient" to "PB User", "city" to "Seoul"))
        },
        endpoint(HttpMethod.Delete, "/addresses/{id}", "Address", "Delete address", roles = setOf(PbRole.USER)) { message("Address deleted.") },
    )
