package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.usedmarket

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod

fun usedMarketOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/used-market/products/{id}/price", "Used Market", "Used market product price") { call ->
            StubResponse(data = mapOf("productId" to call.pathParam("id", "1"), "averagePrice" to 980000, "trend" to "STABLE"))
        },
        endpoint(HttpMethod.Get, "/used-market/categories/{id}/prices", "Used Market", "Used market category prices") {
            paged(listOf(mapOf("categoryId" to 1, "averagePrice" to 540000)))
        },
        endpoint(HttpMethod.Post, "/used-market/pc-builds/{buildId}/estimate", "Used Market", "Used PC build estimate", roles = setOf(PbRole.USER)) { call ->
            StubResponse(data = mapOf("buildId" to call.pathParam("buildId", "1"), "estimatedPrice" to 1200000))
        },
    )
