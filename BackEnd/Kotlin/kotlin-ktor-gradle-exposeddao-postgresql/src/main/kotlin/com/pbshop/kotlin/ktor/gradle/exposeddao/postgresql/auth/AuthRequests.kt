package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthSignupRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String,
)

@Serializable
data class AuthVerifyEmailRequest(
    val email: String,
    val code: String,
)

@Serializable
data class AuthEmailOnlyRequest(
    val email: String,
)

@Serializable
data class AuthLoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class AuthRefreshRequest(
    val refreshToken: String,
)

@Serializable
data class AuthPasswordResetRequest(
    val email: String,
    val phone: String,
)

@Serializable
data class AuthPasswordResetVerifyRequest(
    val email: String,
    val code: String,
)

@Serializable
data class AuthPasswordResetConfirmRequest(
    val resetToken: String,
    val newPassword: String,
)

@Serializable
data class AuthSocialCompleteRequest(
    val phone: String,
    val nickname: String,
)

@Serializable
data class AuthSocialLinkRequest(
    val provider: String,
    val socialToken: String,
)
