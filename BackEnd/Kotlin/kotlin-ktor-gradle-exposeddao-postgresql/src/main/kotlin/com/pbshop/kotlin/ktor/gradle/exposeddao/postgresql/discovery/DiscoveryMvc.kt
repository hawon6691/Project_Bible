package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.discovery

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class DiscoveryRepository : StubDomainRepository(discoveryOperations())

class DiscoveryService(repository: DiscoveryRepository) : StubDomainService(repository)

class DiscoveryController(
    private val service: DiscoveryService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
