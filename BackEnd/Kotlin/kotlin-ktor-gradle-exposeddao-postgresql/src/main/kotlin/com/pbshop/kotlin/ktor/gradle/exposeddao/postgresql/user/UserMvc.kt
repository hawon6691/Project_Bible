package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.user

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class UserRepository : StubDomainRepository(userOperations())

class UserService(repository: UserRepository) : StubDomainService(repository)

class UserController(
    private val service: UserService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
