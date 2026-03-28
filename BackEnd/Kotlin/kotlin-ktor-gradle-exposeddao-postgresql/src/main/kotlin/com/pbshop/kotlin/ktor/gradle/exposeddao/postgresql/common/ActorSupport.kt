package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auth.AuthTokenCodec
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.currentRole
import io.ktor.server.application.ApplicationCall

data class PbActor(
    val userId: Int?,
    val role: PbRole?,
)

fun ApplicationCall.pbActor(): PbActor {
    val bearerToken =
        request.headers["Authorization"]
            ?.removePrefix("Bearer ")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    val claims = bearerToken?.let(AuthTokenCodec::decodeAccessToken)
    val headerRole = currentRole()
    val resolvedRole = claims?.role ?: headerRole
    val resolvedUserId =
        claims?.userId
            ?: request.headers["X-User-Id"]?.toIntOrNull()
            ?: when (resolvedRole) {
                PbRole.ADMIN -> 1
                PbRole.SELLER -> 2
                PbRole.USER -> 4
                null -> null
            }
    return PbActor(userId = resolvedUserId, role = resolvedRole)
}
