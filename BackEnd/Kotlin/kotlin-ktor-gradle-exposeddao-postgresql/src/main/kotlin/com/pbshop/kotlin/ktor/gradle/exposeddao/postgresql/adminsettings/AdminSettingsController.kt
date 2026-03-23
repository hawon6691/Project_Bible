package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.adminsettings

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class AdminSettingsController(
    private val service: AdminSettingsService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
