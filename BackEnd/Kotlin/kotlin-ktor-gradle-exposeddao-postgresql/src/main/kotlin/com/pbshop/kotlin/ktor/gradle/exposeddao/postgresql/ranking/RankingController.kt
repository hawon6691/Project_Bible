package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ranking

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class RankingController(
    private val service: RankingService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
