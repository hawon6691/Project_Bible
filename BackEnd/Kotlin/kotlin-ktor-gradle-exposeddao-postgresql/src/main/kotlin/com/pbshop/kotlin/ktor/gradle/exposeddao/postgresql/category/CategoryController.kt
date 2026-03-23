package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.category

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post

class CategoryController(
    private val service: CategoryService,
) {
    fun Route.register() {
        get("/categories") {
            call.respondStub(service.tree())
        }
        get("/categories/{id}") {
            call.respondStub(service.detail(call.categoryId()))
        }
        post("/categories") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.create(call.receive()))
        }
        patch("/categories/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.update(call.categoryId(), call.receive()))
        }
        delete("/categories/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.delete(call.categoryId()))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.categoryId(): Int = parameters["id"]?.toIntOrNull() ?: 0
}
