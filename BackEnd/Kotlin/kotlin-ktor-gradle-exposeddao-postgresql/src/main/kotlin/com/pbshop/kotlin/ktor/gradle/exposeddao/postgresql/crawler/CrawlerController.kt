package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.crawler

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class CrawlerController(
    private val service: CrawlerService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
