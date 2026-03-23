package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.image

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class ImageController(
    private val service: ImageService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
