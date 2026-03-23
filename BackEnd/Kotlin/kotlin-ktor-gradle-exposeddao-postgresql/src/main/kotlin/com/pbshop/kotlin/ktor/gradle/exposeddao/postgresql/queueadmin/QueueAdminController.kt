package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.queueadmin

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class QueueAdminController(
    private val service: QueueAdminService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
