package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.recommendation

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class RecommendationController(
    private val service: RecommendationService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
