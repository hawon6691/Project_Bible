package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun authOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Post, "/auth/signup", "Auth", "Sign up") {
            StubResponse(
                status = HttpStatusCode.Created,
                data = mapOf("id" to 101, "email" to "user@pbshop.dev", "status" to "PENDING_VERIFICATION"),
            )
        },
        endpoint(HttpMethod.Post, "/auth/verify-email", "Auth", "Verify email") { message("Email verification completed.") },
        endpoint(HttpMethod.Post, "/auth/resend-verification", "Auth", "Resend verification") { message("Verification email resent.") },
        endpoint(HttpMethod.Post, "/auth/login", "Auth", "Login") {
            StubResponse(
                data =
                    mapOf(
                        "accessToken" to "pbshop-access-token",
                        "refreshToken" to "pbshop-refresh-token",
                        "user" to mapOf("id" to 1, "email" to "user@pbshop.dev", "role" to "USER"),
                    ),
            )
        },
        endpoint(HttpMethod.Post, "/auth/password-reset/request", "Auth", "Request password reset") { message("Password reset request accepted.") },
        endpoint(HttpMethod.Post, "/auth/password-reset/verify", "Auth", "Verify password reset token") { message("Password reset token is valid.") },
        endpoint(HttpMethod.Post, "/auth/password-reset/confirm", "Auth", "Confirm password reset") { message("Password reset completed.") },
        endpoint(HttpMethod.Get, "/auth/login/{provider}", "Auth", "Social login redirect") { call ->
            StubResponse(data = mapOf("provider" to call.pathParam("provider", "google"), "redirectUrl" to "https://auth.pbshop.dev/oauth/start"))
        },
        endpoint(HttpMethod.Get, "/auth/callback/{provider}", "Auth", "Social login callback") { call ->
            StubResponse(data = mapOf("provider" to call.pathParam("provider", "google"), "linked" to true))
        },
        endpoint(HttpMethod.Post, "/auth/social/link", "Auth", "Link social account", roles = setOf(PbRole.USER)) { message("Social provider linked.") },
        endpoint(HttpMethod.Delete, "/auth/social/unlink/{provider}", "Auth", "Unlink social account", roles = setOf(PbRole.USER)) { call ->
            message("Social provider unlinked.", "provider" to call.pathParam("provider", "google"))
        },
    )
