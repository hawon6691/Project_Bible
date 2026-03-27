package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.search

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.currentRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post

class SearchController(
    private val service: SearchService,
) {
    fun Route.register() {
        get("/search") {
            call.respondStub(
                service.search(
                    q = call.request.queryParameters["q"],
                    categoryId = call.request.queryParameters["categoryId"]?.toIntOrNull(),
                    minPrice = call.request.queryParameters["minPrice"]?.toIntOrNull(),
                    maxPrice = call.request.queryParameters["maxPrice"]?.toIntOrNull(),
                    specs = call.request.queryParameters["specs"],
                    sort = call.request.queryParameters["sort"],
                    page = call.request.queryParameters["page"]?.toIntOrNull(),
                    limit = call.request.queryParameters["limit"]?.toIntOrNull(),
                ),
            )
        }
        get("/search/autocomplete") {
            call.respondStub(service.autocomplete(call.request.queryParameters["q"]))
        }
        get("/search/popular") {
            call.respondStub(service.popular(call.request.queryParameters["limit"]?.toIntOrNull()))
        }
        post("/search/recent") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.saveRecent(call.currentUserId(), call.receive()))
        }
        get("/search/recent") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.recent(call.currentUserId()))
        }
        delete("/search/recent/{id}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.deleteRecent(call.currentUserId(), call.parameters["id"]?.toIntOrNull() ?: 0))
        }
        delete("/search/recent") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.clearRecent(call.currentUserId()))
        }
        patch("/search/preferences") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.updatePreference(call.currentUserId(), call.receive()))
        }
        get("/search/admin/weights") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.getWeights())
        }
        patch("/search/admin/weights") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.updateWeights(call.receive()))
        }
        get("/search/admin/index/status") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.indexStatus())
        }
        post("/search/admin/index/reindex") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.reindexAll())
        }
        post("/search/admin/index/products/{id}/reindex") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.reindexProduct(call.parameters["id"]?.toIntOrNull() ?: 0))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int =
        request.headers["X-User-Id"]?.toIntOrNull()
            ?: when (currentRole()) {
                PbRole.USER -> 4
                PbRole.ADMIN -> 1
                PbRole.SELLER -> 2
                null -> throw PbShopException(HttpStatusCode.Unauthorized, "AUTH_REQUIRED", "현재 사용자 정보를 확인할 수 없습니다.")
            }
}
