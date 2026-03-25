package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.seller

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

class SellerController(
    private val service: SellerService,
) {
    fun Route.register() {
        get("/sellers") {
            call.respondStub(
                service.listSellers(
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        get("/sellers/{id}") {
            call.respondStub(service.detail(call.sellerId()))
        }
        post("/sellers") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.create(call.receive()))
        }
        patch("/sellers/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.update(call.sellerId(), call.receive()))
        }
        delete("/sellers/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.delete(call.sellerId()))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.sellerId(): Int = parameters["id"]?.toIntOrNull() ?: 0
}
