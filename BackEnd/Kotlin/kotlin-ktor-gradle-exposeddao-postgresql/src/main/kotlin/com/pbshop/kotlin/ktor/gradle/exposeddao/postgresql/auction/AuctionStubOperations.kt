package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auction

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.paged
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun auctionOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Post, "/auctions", "Auction", "Create reverse auction", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "status" to "OPEN"))
        },
        endpoint(HttpMethod.Get, "/auctions", "Auction", "Auction list") {
            paged(listOf(mapOf("id" to 1, "status" to "OPEN", "title" to "Laptop bulk purchase")))
        },
        endpoint(HttpMethod.Get, "/auctions/{id}", "Auction", "Auction detail") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "status" to "OPEN", "bids" to listOf(mapOf("id" to 1, "price" to 1490000))))
        },
        endpoint(HttpMethod.Post, "/auctions/{id}/bids", "Auction", "Create bid", roles = setOf(PbRole.SELLER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "price" to 1490000))
        },
        endpoint(HttpMethod.Patch, "/auctions/{id}/bids/{bidId}/select", "Auction", "Select bid", roles = setOf(PbRole.USER)) {
            message("Bid selected.")
        },
        endpoint(HttpMethod.Delete, "/auctions/{id}", "Auction", "Delete auction", roles = setOf(PbRole.USER)) {
            message("Auction deleted.")
        },
        endpoint(HttpMethod.Patch, "/auctions/{id}/bids/{bidId}", "Auction", "Update bid", roles = setOf(PbRole.SELLER)) { call ->
            StubResponse(data = mapOf("bidId" to call.pathParam("bidId", "1"), "price" to 1480000))
        },
        endpoint(HttpMethod.Delete, "/auctions/{id}/bids/{bidId}", "Auction", "Delete bid", roles = setOf(PbRole.SELLER)) {
            message("Bid deleted.")
        },
    )
