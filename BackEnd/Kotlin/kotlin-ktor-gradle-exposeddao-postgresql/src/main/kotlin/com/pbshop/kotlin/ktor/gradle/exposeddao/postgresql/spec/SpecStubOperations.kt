package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun specOperations() =
    listOf(
        endpoint(HttpMethod.Get, "/specs/definitions", "Spec", "Specification definitions") { message("Spec definitions contract") },
        endpoint(HttpMethod.Post, "/specs/definitions", "Spec", "Create specification definition", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("message" to "created"))
        },
        endpoint(HttpMethod.Patch, "/specs/definitions/{id}", "Spec", "Update specification definition", roles = setOf(PbRole.ADMIN)) { message("Spec definition updated") },
        endpoint(HttpMethod.Delete, "/specs/definitions/{id}", "Spec", "Delete specification definition", roles = setOf(PbRole.ADMIN)) { message("Spec definition deleted") },
        endpoint(HttpMethod.Get, "/products/{id}/specs", "Spec", "Product specifications") { message("Product specs contract") },
        endpoint(HttpMethod.Put, "/products/{id}/specs", "Spec", "Replace product specifications", roles = setOf(PbRole.ADMIN)) { message("Product specs replaced") },
        endpoint(HttpMethod.Post, "/specs/compare", "Spec", "Compare specifications") { message("Spec compare contract") },
        endpoint(HttpMethod.Post, "/specs/compare/scored", "Spec", "Compare specifications with score") { message("Spec scored compare contract") },
        endpoint(HttpMethod.Put, "/specs/scores/{specDefId}", "Spec", "Replace specification score map", roles = setOf(PbRole.ADMIN)) { message("Spec scores replaced") },
    )
