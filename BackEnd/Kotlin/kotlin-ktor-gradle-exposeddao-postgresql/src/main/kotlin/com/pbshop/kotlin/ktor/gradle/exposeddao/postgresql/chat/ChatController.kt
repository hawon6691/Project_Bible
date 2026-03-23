package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.chat

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class ChatController(
    private val service: ChatService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
