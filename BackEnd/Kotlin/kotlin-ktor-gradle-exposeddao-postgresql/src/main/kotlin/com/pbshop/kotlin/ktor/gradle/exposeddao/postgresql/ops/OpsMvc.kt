package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.ops

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class OpsRepository : StubDomainRepository(opsOperations())

class OpsService(repository: OpsRepository) : StubDomainService(repository)

class OpsController(
    private val service: OpsService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
