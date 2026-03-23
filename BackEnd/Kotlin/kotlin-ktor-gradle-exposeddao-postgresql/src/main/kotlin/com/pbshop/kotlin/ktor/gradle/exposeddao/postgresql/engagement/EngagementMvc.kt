package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.engagement

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class EngagementRepository : StubDomainRepository(engagementOperations())

class EngagementService(repository: EngagementRepository) : StubDomainService(repository)

class EngagementController(
    private val service: EngagementService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
