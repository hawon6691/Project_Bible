package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.platform

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondSuccess
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

class PlatformController(
    private val platformService: PlatformService,
) {
    fun Route.registerPublicRoutes() {
        get("/") {
            call.respondSuccess(data = platformService.rootPayload())
        }
    }
}
