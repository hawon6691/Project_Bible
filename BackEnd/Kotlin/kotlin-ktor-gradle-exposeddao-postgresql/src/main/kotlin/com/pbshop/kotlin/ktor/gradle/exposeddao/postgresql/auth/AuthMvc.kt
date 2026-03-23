package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class AuthRepository : StubDomainRepository(authOperations())

class AuthService(repository: AuthRepository) : StubDomainService(repository)

class AuthController(
    private val service: AuthService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
