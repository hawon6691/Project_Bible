package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ranking

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

class RankingController(
    private val service: RankingService,
) {
    fun Route.register() {
        get("/rankings/products/popular") {
            call.respondStub(
                service.popularProducts(
                    categoryId = call.request.queryParameters["categoryId"]?.toIntOrNull(),
                    period = call.request.queryParameters["period"],
                    limit = call.request.queryParameters["limit"]?.toIntOrNull(),
                ),
            )
        }
        get("/rankings/searches") {
            call.respondStub(
                service.popularSearches(
                    period = call.request.queryParameters["period"],
                    limit = call.request.queryParameters["limit"]?.toIntOrNull(),
                ),
            )
        }
    }
}
