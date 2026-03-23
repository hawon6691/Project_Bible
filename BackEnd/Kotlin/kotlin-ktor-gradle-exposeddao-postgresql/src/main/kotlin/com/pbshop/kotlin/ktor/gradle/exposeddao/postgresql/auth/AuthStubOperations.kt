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
                data = mapOf("id" to 101, "email" to "user@pbshop.dev", "name" to "PB User", "message" to "인증 메일이 발송되었습니다. 이메일을 확인해주세요."),
            )
        },
        endpoint(HttpMethod.Post, "/auth/verify-email", "Auth", "Verify email") { message("이메일 인증이 완료되었습니다.", "verified" to true) },
        endpoint(HttpMethod.Post, "/auth/resend-verification", "Auth", "Resend verification") { message("인증 메일이 재발송되었습니다.") },
        endpoint(HttpMethod.Post, "/auth/login", "Auth", "Login") {
            StubResponse(
                data =
                    mapOf(
                        "accessToken" to "pbshop-access-token",
                        "refreshToken" to "pbshop-refresh-token",
                        "expiresIn" to 1800,
                    ),
            )
        },
        endpoint(HttpMethod.Post, "/auth/logout", "Auth", "Logout", roles = setOf(PbRole.USER)) { message("로그아웃되었습니다.") },
        endpoint(HttpMethod.Post, "/auth/refresh", "Auth", "Refresh token") {
            StubResponse(
                data =
                    mapOf(
                        "accessToken" to "pbshop-access-token",
                        "refreshToken" to "pbshop-refresh-token",
                        "expiresIn" to 1800,
                    ),
            )
        },
        endpoint(HttpMethod.Post, "/auth/password-reset/request", "Auth", "Request password reset") { message("비밀번호 재설정 인증 메일이 발송되었습니다.") },
        endpoint(HttpMethod.Post, "/auth/password-reset/verify", "Auth", "Verify password reset token") { message("Password reset token is valid.", "resetToken" to "pbshop-reset-token") },
        endpoint(HttpMethod.Post, "/auth/password-reset/confirm", "Auth", "Confirm password reset") { message("비밀번호가 성공적으로 변경되었습니다.") },
        endpoint(HttpMethod.Get, "/auth/login/{provider}", "Auth", "Social login redirect") { call ->
            StubResponse(data = mapOf("provider" to call.pathParam("provider", "google"), "redirectUrl" to "https://auth.pbshop.dev/oauth/start"))
        },
        endpoint(HttpMethod.Get, "/auth/callback/{provider}", "Auth", "Social login callback") { call ->
            StubResponse(data = mapOf("provider" to call.pathParam("provider", "google"), "accessToken" to "pbshop-access-token", "refreshToken" to "pbshop-refresh-token", "expiresIn" to 1800, "isNewUser" to false))
        },
        endpoint(HttpMethod.Post, "/auth/social/complete", "Auth", "Complete social signup") { message("소셜 가입이 완료되었습니다.", "accessToken" to "pbshop-access-token", "refreshToken" to "pbshop-refresh-token") },
        endpoint(HttpMethod.Post, "/auth/social/link", "Auth", "Link social account", roles = setOf(PbRole.USER)) { message("Social provider linked.") },
        endpoint(HttpMethod.Delete, "/auth/social/unlink/{provider}", "Auth", "Unlink social account", roles = setOf(PbRole.USER)) { call ->
            message("Social provider unlinked.", "provider" to call.pathParam("provider", "google"))
        },
    )
