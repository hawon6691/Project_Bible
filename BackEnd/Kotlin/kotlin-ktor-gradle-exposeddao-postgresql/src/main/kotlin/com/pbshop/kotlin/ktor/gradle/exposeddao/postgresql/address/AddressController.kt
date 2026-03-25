package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.address

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

class AddressController(
    private val service: AddressService,
) {
    fun Route.register() {
        get("/addresses") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.list(call.currentUserId()))
        }
        post("/addresses") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.create(call.currentUserId(), call.receive()))
        }
        patch("/addresses/{id}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.update(call.currentUserId(), call.addressId(), call.receive()))
        }
        delete("/addresses/{id}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.delete(call.currentUserId(), call.addressId()))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.addressId(): Int = parameters["id"]?.toIntOrNull() ?: 0

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int =
        request.headers["X-User-Id"]?.toIntOrNull()
            ?: when (currentRole()) {
                PbRole.USER -> 4
                PbRole.ADMIN -> 1
                PbRole.SELLER -> 2
                null -> throw PbShopException(HttpStatusCode.Unauthorized, "AUTH_REQUIRED", "현재 사용자 정보를 확인할 수 없습니다.")
            }
}
