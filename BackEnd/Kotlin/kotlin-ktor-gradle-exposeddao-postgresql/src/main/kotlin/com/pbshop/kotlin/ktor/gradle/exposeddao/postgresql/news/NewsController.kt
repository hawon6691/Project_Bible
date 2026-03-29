package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.news

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

class NewsController(
    private val service: NewsService,
) {
    fun Route.register() {
        get("/news/categories") {
            call.respondStub(service.categories())
        }
        post("/news/categories") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.createCategory(call.receive()))
        }
        delete("/news/categories/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.deleteCategory(call.pathInt("id")))
        }
        get("/news/{id}") {
            call.respondStub(service.detail(call.pathInt("id")))
        }
        get("/news") {
            call.respondStub(
                service.list(
                    category = call.request.queryParameters["category"],
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        post("/news") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.create(call.receive()))
        }
        patch("/news/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.update(call.pathInt("id"), call.receive()))
        }
        delete("/news/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.delete(call.pathInt("id")))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.pathInt(name: String): Int = parameters[name]?.toIntOrNull() ?: 0
}
