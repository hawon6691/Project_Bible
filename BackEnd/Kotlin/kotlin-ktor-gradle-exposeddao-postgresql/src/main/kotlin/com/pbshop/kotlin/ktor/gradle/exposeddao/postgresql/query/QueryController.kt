package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.query

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

class QueryController(
    private val service: QueryService,
) {
    fun Route.register() {
        get("/query/products") {
            call.respondStub(
                service.listProducts(
                    ProductQueryViewQuery(
                        categoryId = call.request.queryParameters["categoryId"]?.toIntOrNull(),
                        keyword = call.request.queryParameters["keyword"],
                        minPrice = call.request.queryParameters["minPrice"]?.toIntOrNull(),
                        maxPrice = call.request.queryParameters["maxPrice"]?.toIntOrNull(),
                        sort = ProductQuerySort.fromQuery(call.request.queryParameters["sort"]),
                        page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                        limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                    ),
                ),
            )
        }
        get("/query/products/{productId}") {
            call.respondStub(service.detail(call.parameters["productId"]?.toIntOrNull() ?: 0))
        }
        post("/admin/query/products/{productId}/sync") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.syncProduct(call.parameters["productId"]?.toIntOrNull() ?: 0))
        }
        post("/admin/query/products/rebuild") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.rebuildAll())
        }
    }
}
