package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.errorcode

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

class ErrorCodeController(
    private val service: ErrorCodeService,
) {
    fun Route.register() {
        get("/errors/codes") {
            call.respondStub(service.list())
        }
        get("/errors/codes/{key}") {
            call.respondStub(service.detail(call.parameters["key"].orEmpty()))
        }
    }
}
