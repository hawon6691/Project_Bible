package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod

fun userOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/users/me", "User", "Current user", roles = setOf(PbRole.USER)) {
            StubResponse(
                data =
                    mapOf(
                        "id" to 1,
                        "email" to "user@pbshop.dev",
                        "name" to "PB User",
                        "role" to "USER",
                        "badges" to listOf("REVIEW_MASTER"),
                    ),
            )
        },
        endpoint(HttpMethod.Get, "/users", "User", "Admin user list", roles = setOf(PbRole.ADMIN)) {
            paged(
                listOf(
                    mapOf("id" to 1, "email" to "user@pbshop.dev", "role" to "USER"),
                    mapOf("id" to 2, "email" to "seller@pbshop.dev", "role" to "SELLER"),
                ),
            )
        },
    )
