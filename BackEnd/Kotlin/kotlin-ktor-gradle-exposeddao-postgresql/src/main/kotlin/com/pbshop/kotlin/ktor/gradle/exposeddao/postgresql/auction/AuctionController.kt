package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auction

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.pbActor
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post

class AuctionController(
    private val service: AuctionService,
) {
    fun Route.register() {
        post("/auctions") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.createAuction(call.currentUserId(), call.receive()))
        }
        get("/auctions") {
            call.respondStub(
                service.listAuctions(
                    status = call.request.queryParameters["status"],
                    categoryId = call.request.queryParameters["categoryId"]?.toIntOrNull(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        get("/auctions/{id}") {
            call.respondStub(service.detail(call.pathInt("id")))
        }
        post("/auctions/{id}/bids") {
            call.requireAnyRole(PbRole.SELLER)
            call.respondStub(service.createBid(call.currentUserId(), call.pathInt("id"), call.receive()))
        }
        patch("/auctions/{id}/bids/{bidId}/select") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.selectBid(call.currentUserId(), call.pathInt("id"), call.pathInt("bidId")))
        }
        delete("/auctions/{id}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.cancelAuction(call.currentUserId(), call.pathInt("id")))
        }
        patch("/auctions/{id}/bids/{bidId}") {
            call.requireAnyRole(PbRole.SELLER)
            call.respondStub(service.updateBid(call.currentUserId(), call.pathInt("id"), call.pathInt("bidId"), call.receive()))
        }
        delete("/auctions/{id}/bids/{bidId}") {
            call.requireAnyRole(PbRole.SELLER)
            call.respondStub(service.deleteBid(call.currentUserId(), call.pathInt("id"), call.pathInt("bidId")))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int = pbActor().userId ?: 0

    private fun io.ktor.server.application.ApplicationCall.pathInt(name: String): Int = parameters[name]?.toIntOrNull() ?: 0
}
