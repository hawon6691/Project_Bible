package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.community

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class CommunityController(
    private val service: CommunityService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
