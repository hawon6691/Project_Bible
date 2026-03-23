package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.point

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun pointOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/points/balance", "Point", "Point balance", roles = setOf(PbRole.USER)) {
            StubResponse(data = mapOf("balance" to 15000, "expiringSoon" to 3000, "expiringDate" to "2026-03-01"))
        },
        endpoint(HttpMethod.Get, "/points/transactions", "Point", "Point transactions", roles = setOf(PbRole.USER)) {
            paged(listOf(mapOf("id" to 1, "type" to "EARN", "amount" to 500, "balance" to 15000)))
        },
        endpoint(HttpMethod.Post, "/admin/points/grant", "Point", "Grant points", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "type" to "ADMIN_GRANT", "amount" to 1000))
        },
    )
