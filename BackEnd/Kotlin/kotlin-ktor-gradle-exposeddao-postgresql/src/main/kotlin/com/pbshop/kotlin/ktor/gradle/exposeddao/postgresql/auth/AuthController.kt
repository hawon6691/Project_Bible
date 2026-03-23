package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class AuthController(
    private val service: AuthService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
