package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.builder

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class BuilderRepository : StubDomainRepository(builderOperations())

class BuilderService(repository: BuilderRepository) : StubDomainService(repository)

class BuilderController(
    private val service: BuilderService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
