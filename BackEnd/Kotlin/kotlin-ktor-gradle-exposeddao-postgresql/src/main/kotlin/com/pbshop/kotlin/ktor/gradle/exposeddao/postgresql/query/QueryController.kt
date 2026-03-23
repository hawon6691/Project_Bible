package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.query

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class QueryController(
    private val service: QueryService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
