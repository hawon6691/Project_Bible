package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.compare

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post

class CompareController(
    private val service: CompareService,
) {
    fun Route.register() {
        post("/compare/add") {
            call.respondStub(service.add(call.compareKey(), call.receive<AddCompareItemRequest>().productId))
        }
        delete("/compare/{productId}") {
            call.respondStub(service.remove(call.compareKey(), call.parameters["productId"]?.toIntOrNull() ?: 0))
        }
        get("/compare") {
            call.respondStub(service.list(call.compareKey()))
        }
        get("/compare/detail") {
            call.respondStub(service.detail(call.compareKey()))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.compareKey(): String =
        request.headers["X-Compare-Key"]?.trim().takeUnless { it.isNullOrBlank() } ?: "guest"
}
