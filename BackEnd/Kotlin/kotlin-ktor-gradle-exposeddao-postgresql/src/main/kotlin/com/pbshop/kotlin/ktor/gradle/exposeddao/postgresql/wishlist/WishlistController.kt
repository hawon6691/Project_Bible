package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.wishlist

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.registerEndpointSpecs
import io.ktor.server.routing.Route

class WishlistController(
    private val service: WishlistService,
) {
    fun Route.register() {
        registerEndpointSpecs(service.specs, service::execute)
    }
}
