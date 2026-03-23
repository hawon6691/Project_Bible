package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class ReviewController(
    private val service: ReviewService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
