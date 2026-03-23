package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.pcbuilder

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.productSummary
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun pcBuilderOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/pc-builds", "PC Builder", "PC build list", roles = setOf(PbRole.USER)) {
            paged(listOf(mapOf("id" to 1, "name" to "Gaming PC 2026", "purpose" to "GAMING")))
        },
        endpoint(HttpMethod.Post, "/pc-builds", "PC Builder", "Create PC build", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "name" to "Gaming PC 2026", "totalPrice" to 0))
        },
        endpoint(HttpMethod.Get, "/pc-builds/{id}", "PC Builder", "PC build detail") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "parts" to emptyList<String>(), "totalPrice" to 350000))
        },
        endpoint(HttpMethod.Patch, "/pc-builds/{id}", "PC Builder", "Update PC build", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "name" to "Updated build"))
        },
        endpoint(HttpMethod.Delete, "/pc-builds/{id}", "PC Builder", "Delete PC build", roles = setOf(PbRole.USER)) {
            message("PC build deleted.")
        },
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
        endpoint(HttpMethod.Get, "/pc-builds/popular", "PC Builder", "Popular PC builds") {
            paged(listOf(mapOf("id" to 1, "name" to "Gaming PC 2026", "likes" to 120)))
        },
        endpoint(HttpMethod.Get, "/admin/compatibility-rules", "PC Builder", "Compatibility rule list", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = listOf(mapOf("id" to 1, "name" to "Socket match", "enabled" to true)))
        },
        endpoint(HttpMethod.Post, "/admin/compatibility-rules", "PC Builder", "Create compatibility rule", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "name" to "Power headroom"))
        },
        endpoint(HttpMethod.Patch, "/admin/compatibility-rules/{id}", "PC Builder", "Update compatibility rule", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "name" to "Updated compatibility rule"))
        },
        endpoint(HttpMethod.Delete, "/admin/compatibility-rules/{id}", "PC Builder", "Delete compatibility rule", roles = setOf(PbRole.ADMIN)) {
            message("Compatibility rule deleted.")
        },
    )
