package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.fraud

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.pbActor
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch

class FraudController(
    private val service: FraudService,
) {
    fun Route.register() {
        get("/fraud/alerts") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(
                service.alerts(
                    status = call.request.queryParameters["status"],
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                )
            )
        }
        patch("/fraud/alerts/{id}/approve") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.approve(call.pathInt("id"), call.currentUserId()))
        }
        patch("/fraud/alerts/{id}/reject") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.reject(call.pathInt("id"), call.currentUserId()))
        }
        get("/products/{id}/real-price") {
            call.respondStub(
                service.realPrice(
                    productId = call.pathInt("id"),
                    sellerId = call.request.queryParameters["sellerId"]?.toIntOrNull(),
                )
            )
        }
    }

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int = pbActor().userId ?: 0

    private fun io.ktor.server.application.ApplicationCall.pathInt(name: String): Int = parameters[name]?.toIntOrNull() ?: 0
}
