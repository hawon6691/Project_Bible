package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun categoryOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/categories", "Category", "Category list") {
            StubResponse(data = listOf(mapOf("id" to 1, "name" to "Laptop", "slug" to "laptop"), mapOf("id" to 2, "name" to "PC Parts", "slug" to "pc-parts")))
        },
        endpoint(HttpMethod.Post, "/categories", "Category", "Create category", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 3, "name" to "Tablet", "slug" to "tablet"))
        },
    )
