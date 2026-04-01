package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.usedmarket

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.pbActor
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

class UsedMarketController(
    private val service: UsedMarketService,
) {
    fun Route.register() {
        get("/used-market/products/{id}/price") {
            call.respondStub(service.productPrice(call.pathInt("id")))
        }
        get("/used-market/categories/{id}/prices") {
            call.respondStub(
                service.categoryPrices(
                    categoryId = call.pathInt("id"),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                )
            )
        }
        post("/used-market/pc-builds/{buildId}/estimate") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.estimateBuild(call.currentUserId(), call.pathInt("buildId")))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int = pbActor().userId ?: 0

    private fun io.ktor.server.application.ApplicationCall.pathInt(name: String): Int = parameters[name]?.toIntOrNull() ?: 0
}
