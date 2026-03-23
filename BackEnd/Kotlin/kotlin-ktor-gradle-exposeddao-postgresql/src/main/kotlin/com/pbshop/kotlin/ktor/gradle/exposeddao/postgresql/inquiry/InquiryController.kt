package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.inquiry

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class InquiryController(
    private val service: InquiryService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
