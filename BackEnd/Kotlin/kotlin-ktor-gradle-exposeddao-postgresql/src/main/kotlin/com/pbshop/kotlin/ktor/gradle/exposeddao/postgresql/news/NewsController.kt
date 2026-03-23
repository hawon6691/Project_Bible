package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.news

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class NewsController(
    private val service: NewsService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
