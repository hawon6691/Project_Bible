package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import java.time.Instant

enum class AuthUserStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED,
}

enum class AuthVerificationType {
    SIGNUP,
    PASSWORD_RESET,
}

data class AuthUserRecord(
    val id: Int,
    val email: String,
    val passwordHash: String,
    val name: String,
    val phone: String,
    val role: PbRole,
    val status: AuthUserStatus,
    val emailVerified: Boolean,
    val emailVerifiedAt: Instant?,
    val nickname: String,
    val refreshToken: String?,
)

data class AuthVerificationRecord(
    val id: Int,
    val userId: Int,
    val type: AuthVerificationType,
    val code: String,
    val attemptCount: Int,
    val isUsed: Boolean,
    val expiresAt: Instant,
    val createdAt: Instant,
)

data class NewAuthUser(
    val email: String,
    val passwordHash: String,
    val name: String,
    val phone: String,
    val nickname: String,
    val role: PbRole = PbRole.USER,
    val status: AuthUserStatus = AuthUserStatus.ACTIVE,
    val emailVerified: Boolean = false,
)

interface AuthRepository {
    fun createUser(newUser: NewAuthUser): AuthUserRecord

    fun findUserByEmail(email: String): AuthUserRecord?

    fun findUserById(id: Int): AuthUserRecord?

    fun findUserByRefreshToken(refreshToken: String): AuthUserRecord?

    fun saveRefreshToken(
        userId: Int,
        refreshToken: String?,
    )

    fun markEmailVerified(
        userId: Int,
        verifiedAt: Instant,
    )

    fun updatePassword(
        userId: Int,
        passwordHash: String,
    )

    fun createVerification(
        userId: Int,
        type: AuthVerificationType,
        code: String,
        expiresAt: Instant,
    ): AuthVerificationRecord

    fun findLatestActiveVerification(
        userId: Int,
        type: AuthVerificationType,
    ): AuthVerificationRecord?

    fun findVerificationById(id: Int): AuthVerificationRecord?

    fun updateVerificationAttemptCount(
        verificationId: Int,
        attemptCount: Int,
    )

    fun markVerificationUsed(verificationId: Int)
}
