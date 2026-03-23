package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.friend

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class FriendController(
    private val service: FriendService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
