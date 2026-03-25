package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price

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

class PriceController(
    private val service: PriceService,
) {
    fun Route.register() {
        get("/products/{id}/prices") {
            call.respondStub(service.listProductPrices(call.productId()))
        }
        post("/products/{id}/prices") {
            call.requireAnyRole(PbRole.SELLER, PbRole.ADMIN)
            call.respondStub(service.createPriceEntry(call.productId(), call.receive()))
        }
        patch("/prices/{id}") {
            call.requireAnyRole(PbRole.SELLER, PbRole.ADMIN)
            call.respondStub(service.updatePriceEntry(call.priceId(), call.receive()))
        }
        delete("/prices/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.deletePriceEntry(call.priceId()))
        }
        get("/products/{id}/price-history") {
            call.respondStub(service.priceHistory(call.productId()))
        }
        get("/price-alerts") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(
                service.listAlerts(
                    userId = call.currentUserId(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        post("/price-alerts") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.createAlert(call.currentUserId(), call.receive()))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.productId(): Int = parameters["id"]?.toIntOrNull() ?: 0

    private fun io.ktor.server.application.ApplicationCall.priceId(): Int = parameters["id"]?.toIntOrNull() ?: 0

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int =
        request.headers["X-User-Id"]?.toIntOrNull()
            ?: when (currentRole()) {
                PbRole.USER -> 4
                PbRole.SELLER -> 2
                PbRole.ADMIN -> 1
                null -> throw PbShopException(HttpStatusCode.Unauthorized, "AUTH_REQUIRED", "현재 사용자 정보를 확인할 수 없습니다.")
            }
}
