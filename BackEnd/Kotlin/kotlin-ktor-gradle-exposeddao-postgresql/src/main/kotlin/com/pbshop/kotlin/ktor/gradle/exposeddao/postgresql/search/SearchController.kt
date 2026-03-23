package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class SearchController(
    private val service: SearchService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
