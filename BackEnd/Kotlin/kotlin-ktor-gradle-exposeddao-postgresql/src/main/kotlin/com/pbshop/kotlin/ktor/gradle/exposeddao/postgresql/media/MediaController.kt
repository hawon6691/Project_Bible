package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class MediaController(
    private val service: MediaService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
