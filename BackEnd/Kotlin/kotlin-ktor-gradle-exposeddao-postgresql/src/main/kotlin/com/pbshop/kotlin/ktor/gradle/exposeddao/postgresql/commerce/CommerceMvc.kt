package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.commerce

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class CommerceRepository : StubDomainRepository(commerceOperations())

class CommerceService(repository: CommerceRepository) : StubDomainService(repository)

class CommerceController(
    private val service: CommerceService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
