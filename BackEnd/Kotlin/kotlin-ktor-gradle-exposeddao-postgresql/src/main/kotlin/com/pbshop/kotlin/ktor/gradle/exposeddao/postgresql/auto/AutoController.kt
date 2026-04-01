package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auto

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

class AutoController(
    private val service: AutoService,
) {
    fun Route.register() {
        get("/auto/models") {
            call.respondStub(
                service.models(
                    brand = call.request.queryParameters["brand"],
                    type = call.request.queryParameters["type"],
                ),
            )
        }
        get("/auto/models/{id}/trims") {
            call.respondStub(service.trims(call.pathInt("id")))
        }
        post("/auto/estimate") {
            call.respondStub(service.estimate(call.receive()))
        }
        get("/auto/models/{id}/lease-offers") {
            call.respondStub(service.leaseOffers(call.pathInt("id")))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.pathInt(name: String): Int = parameters[name]?.toIntOrNull() ?: 0
}
