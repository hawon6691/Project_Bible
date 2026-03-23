package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class UserController(
    private val service: UserService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
