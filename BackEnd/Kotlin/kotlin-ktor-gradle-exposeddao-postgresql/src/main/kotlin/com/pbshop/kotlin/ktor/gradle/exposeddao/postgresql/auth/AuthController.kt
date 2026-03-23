package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post

class AuthController(
    private val service: AuthService,
) {
    fun Route.register() {
        post("/auth/signup") {
            call.respondStub(service.signup(call.receive()))
        }
        post("/auth/verify-email") {
            call.respondStub(service.verifyEmail(call.receive()))
        }
        post("/auth/resend-verification") {
            call.respondStub(service.resendVerification(call.receive()))
        }
        post("/auth/login") {
            call.respondStub(service.login(call.receive()))
        }
        post("/auth/logout") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.logout(call.bearerToken()))
        }
        post("/auth/refresh") {
            call.respondStub(service.refresh(call.receive()))
        }
        post("/auth/password-reset/request") {
            call.respondStub(service.passwordResetRequest(call.receive()))
        }
        post("/auth/password-reset/verify") {
            call.respondStub(service.passwordResetVerify(call.receive()))
        }
        post("/auth/password-reset/confirm") {
            call.respondStub(service.passwordResetConfirm(call.receive()))
        }
        get("/auth/login/{provider}") {
            call.respondStub(service.socialLoginRedirect(call.parameters["provider"].orEmpty()))
        }
        get("/auth/callback/{provider}") {
            call.respondStub(
                service.socialCallback(
                    provider = call.parameters["provider"].orEmpty(),
                    code = call.request.queryParameters["code"],
                    state = call.request.queryParameters["state"],
                ),
            )
        }
        post("/auth/social/complete") {
            call.respondStub(service.socialComplete(call.receive()))
        }
        post("/auth/social/link") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.socialLink(call.receive()))
        }
        delete("/auth/social/unlink/{provider}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.socialUnlink(call.parameters["provider"].orEmpty()))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.bearerToken(): String? =
        request.headers["Authorization"]
            ?.removePrefix("Bearer ")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
}
