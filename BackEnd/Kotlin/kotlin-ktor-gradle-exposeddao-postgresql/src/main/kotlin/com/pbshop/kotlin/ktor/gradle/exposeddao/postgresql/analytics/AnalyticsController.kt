package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.analytics

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

class AnalyticsController(
    private val service: AnalyticsService,
) {
    fun Route.register() {
        get("/analytics/products/{id}/lowest-ever") {
            call.respondStub(service.lowestEver(call.pathInt("id")))
        }
        get("/analytics/products/{id}/unit-price") {
            call.respondStub(service.unitPrice(call.pathInt("id")))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.pathInt(name: String): Int = parameters[name]?.toIntOrNull() ?: 0
}
