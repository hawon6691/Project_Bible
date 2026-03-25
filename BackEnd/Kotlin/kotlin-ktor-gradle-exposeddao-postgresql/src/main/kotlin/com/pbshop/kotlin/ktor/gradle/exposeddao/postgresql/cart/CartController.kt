package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.cart

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

class CartController(
    private val service: CartService,
) {
    fun Route.register() {
        get("/cart") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.list(call.currentUserId()))
        }
        post("/cart") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.add(call.currentUserId(), call.receive()))
        }
        patch("/cart/{itemId}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.updateQuantity(call.currentUserId(), call.itemId(), call.receive()))
        }
        delete("/cart/{itemId}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.delete(call.currentUserId(), call.itemId()))
        }
        delete("/cart") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.clear(call.currentUserId()))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.itemId(): Int = parameters["itemId"]?.toIntOrNull() ?: 0

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int =
        request.headers["X-User-Id"]?.toIntOrNull()
            ?: when (currentRole()) {
                PbRole.USER -> 4
                PbRole.ADMIN -> 1
                PbRole.SELLER -> 2
                null -> throw PbShopException(HttpStatusCode.Unauthorized, "AUTH_REQUIRED", "현재 사용자 정보를 확인할 수 없습니다.")
            }
}
