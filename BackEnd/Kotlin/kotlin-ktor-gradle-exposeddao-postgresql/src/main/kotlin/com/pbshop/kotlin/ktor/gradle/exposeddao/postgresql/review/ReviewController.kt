package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.review

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

class ReviewController(
    private val service: ReviewService,
) {
    fun Route.register() {
        get("/products/{productId}/reviews") {
            call.respondStub(
                service.listProductReviews(
                    productId = call.productId(),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        post("/products/{productId}/reviews") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.createReview(call.currentUserId(), call.productId(), call.receive()))
        }
        patch("/reviews/{id}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.updateReview(call.currentUserId(), call.reviewId(), call.receive()))
        }
        delete("/reviews/{id}") {
            call.requireAnyRole(PbRole.USER, PbRole.ADMIN)
            val role = call.currentRole()
            call.respondStub(
                service.deleteReview(
                    userId = if (role == PbRole.ADMIN) null else call.currentUserId(),
                    isAdmin = role == PbRole.ADMIN,
                    reviewId = call.reviewId(),
                ),
            )
        }
    }

    private fun io.ktor.server.application.ApplicationCall.productId(): Int = parameters["productId"]?.toIntOrNull() ?: 0

    private fun io.ktor.server.application.ApplicationCall.reviewId(): Int = parameters["id"]?.toIntOrNull() ?: 0

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int =
        request.headers["X-User-Id"]?.toIntOrNull()
            ?: when (currentRole()) {
                PbRole.USER -> 4
                PbRole.ADMIN -> 1
                PbRole.SELLER -> 2
                null -> throw PbShopException(HttpStatusCode.Unauthorized, "AUTH_REQUIRED", "현재 사용자 정보를 확인할 수 없습니다.")
            }
}
