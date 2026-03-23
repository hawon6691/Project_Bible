package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class AccessTokenClaims(
    val userId: Int,
    val email: String,
    val role: PbRole,
    val expiresAtEpochSeconds: Long,
)

data class ResetTokenClaims(
    val userId: Int,
    val email: String,
    val verificationId: Int,
    val code: String,
    val expiresAtEpochSeconds: Long,
)

object AuthTokenCodec {
    private const val secret = "pbshop-kotlin-ktor-auth-secret"
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun createAccessToken(
        userId: Int,
        email: String,
        role: PbRole,
        expiresAt: Instant,
    ): String = issue("access|$userId|$email|${role.name}|${expiresAt.epochSecond}")

    fun decodeAccessToken(token: String): AccessTokenClaims? {
        val decoded = decode(token) ?: return null
        val parts = decoded.split('|')
        if (parts.size != 5 || parts[0] != "access") {
            return null
        }
        val expiresAt = parts[4].toLongOrNull() ?: return null
        if (expiresAt <= Instant.now().epochSecond) {
            return null
        }
        val role = PbRole.fromHeader(parts[3]) ?: return null
        return AccessTokenClaims(
            userId = parts[1].toIntOrNull() ?: return null,
            email = parts[2],
            role = role,
            expiresAtEpochSeconds = expiresAt,
        )
    }

    fun createRefreshToken(
        userId: Int,
        email: String,
        role: PbRole,
        expiresAt: Instant,
    ): String = issue("refresh|$userId|$email|${role.name}|${expiresAt.epochSecond}")

    fun createResetToken(
        userId: Int,
        email: String,
        verificationId: Int,
        code: String,
        expiresAt: Instant,
    ): String = issue("reset|$userId|$email|$verificationId|$code|${expiresAt.epochSecond}")

    fun decodeResetToken(token: String): ResetTokenClaims? {
        val decoded = decode(token) ?: return null
        val parts = decoded.split('|')
        if (parts.size != 6 || parts[0] != "reset") {
            return null
        }
        val expiresAt = parts[5].toLongOrNull() ?: return null
        if (expiresAt <= Instant.now().epochSecond) {
            return null
        }
        return ResetTokenClaims(
            userId = parts[1].toIntOrNull() ?: return null,
            email = parts[2],
            verificationId = parts[3].toIntOrNull() ?: return null,
            code = parts[4],
            expiresAtEpochSeconds = expiresAt,
        )
    }

    private fun issue(payload: String): String {
        val encodedPayload = encoder.encodeToString(payload.toByteArray(StandardCharsets.UTF_8))
        val signature = hmac(encodedPayload)
        return "$encodedPayload.$signature"
    }

    private fun decode(token: String): String? {
        val parts = token.split('.')
        if (parts.size != 2) {
            return null
        }
        val payload = parts[0]
        val signature = parts[1]
        if (hmac(payload) != signature) {
            return null
        }
        return runCatching {
            String(decoder.decode(payload), StandardCharsets.UTF_8)
        }.getOrNull()
    }

    private fun hmac(payload: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        return encoder.encodeToString(mac.doFinal(payload.toByteArray(StandardCharsets.UTF_8)))
    }
}
