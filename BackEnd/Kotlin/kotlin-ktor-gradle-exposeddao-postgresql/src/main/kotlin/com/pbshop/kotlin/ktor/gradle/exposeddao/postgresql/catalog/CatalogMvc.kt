package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.catalog

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainRepository
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubDomainService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class CatalogRepository : StubDomainRepository(catalogOperations())

class CatalogService(repository: CatalogRepository) : StubDomainService(repository)

class CatalogController(
    private val service: CatalogService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
