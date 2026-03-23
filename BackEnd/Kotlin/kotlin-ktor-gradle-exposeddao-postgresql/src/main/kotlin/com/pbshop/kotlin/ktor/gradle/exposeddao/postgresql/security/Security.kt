package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.util.AttributeKey

val RequestIdAttributeKey = AttributeKey<String>("pbshop.requestId")

enum class PbRole {
    USER,
    SELLER,
    ADMIN,
    ;

    companion object {
        fun fromHeader(value: String?): PbRole? =
            value
                ?.trim()
                ?.uppercase()
                ?.let { candidate ->
                    entries.firstOrNull { role -> role.name == candidate }
                }
    }
}

fun ApplicationCall.requestId(): String = attributes[RequestIdAttributeKey]

fun ApplicationCall.currentRole(): PbRole? = PbRole.fromHeader(request.headers["X-Role"])

fun ApplicationCall.requireAnyRole(vararg allowed: PbRole) {
    val callerRole = currentRole()
    if (allowed.isEmpty()) {
        return
    }
    if (callerRole == null) {
        throw PbShopException(
            status = HttpStatusCode.Unauthorized,
            code = "AUTH_REQUIRED",
            message = "This endpoint requires an authenticated PBShop role header.",
        )
    }
    if (callerRole == PbRole.ADMIN || allowed.contains(callerRole)) {
        return
    }
    throw PbShopException(
        status = HttpStatusCode.Forbidden,
        code = "FORBIDDEN",
        message = "The current role is not allowed to access this endpoint.",
    )
}
