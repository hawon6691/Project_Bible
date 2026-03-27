package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.prediction

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

class PredictionController(
    private val service: PredictionService,
) {
    fun Route.register() {
        get("/predictions/products/{productId}/price-trend") {
            call.respondStub(
                service.priceTrend(
                    productId = call.parameters["productId"]?.toIntOrNull() ?: 0,
                    days = call.request.queryParameters["days"]?.toIntOrNull(),
                ),
            )
        }
    }
}
