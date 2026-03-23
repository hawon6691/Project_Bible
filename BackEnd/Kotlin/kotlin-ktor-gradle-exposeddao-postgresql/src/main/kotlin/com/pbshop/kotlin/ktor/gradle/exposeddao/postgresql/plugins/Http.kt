package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondFailure
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.RequestIdAttributeKey
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.header
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val logger = LoggerFactory.getLogger("pbshop-http")
private val rateLimitWindows = ConcurrentHashMap<String, RateLimitWindow>()

private data class RateLimitWindow(
    var startedAtMillis: Long,
    var count: Int,
)

fun Application.configureHttp(config: PbShopConfig) {
    install(DefaultHeaders) {
        header("X-Content-Type-Options", "nosniff")
        header("X-Frame-Options", "DENY")
        header("Referrer-Policy", "strict-origin-when-cross-origin")
        header("Permissions-Policy", "camera=(), microphone=(), geolocation=()")
        header("Cross-Origin-Opener-Policy", "same-origin")
        header("Cross-Origin-Resource-Policy", "same-origin")
        header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
    }

    install(StatusPages) {
        exception<PbShopException> { call, cause ->
            call.respondFailure(cause.status, cause.code, cause.message)
        }
        exception<Throwable> { call, cause ->
            logger.error(
                "Unhandled exception requestId={} path={}",
                call.attributes.getOrNull(RequestIdAttributeKey),
                call.request.path(),
                cause,
            )
            call.respondFailure(
                status = io.ktor.http.HttpStatusCode.InternalServerError,
                code = "INTERNAL_SERVER_ERROR",
                message = "An unexpected error occurred.",
            )
        }
    }

    intercept(ApplicationCallPipeline.Setup) {
        val requestIdHeader = config.security.requestIdHeader
        val requestId =
            call.request.header(requestIdHeader)
                ?.takeIf { it.isNotBlank() }
                ?: UUID.randomUUID().toString()
        call.attributes.put(RequestIdAttributeKey, requestId)
        call.response.headers.append(requestIdHeader, requestId)
        proceed()
    }

    intercept(ApplicationCallPipeline.Plugins) {
        enforceRateLimit(call, config)
        proceed()
    }

    intercept(ApplicationCallPipeline.Monitoring) {
        val startedAt = System.currentTimeMillis()
        proceed()
        val durationMs = System.currentTimeMillis() - startedAt
        val statusCode = call.response.status()?.value ?: 200
        val level =
            when {
                statusCode >= 500 -> "ERROR"
                statusCode >= 400 -> "WARN"
                else -> "INFO"
            }
        logger.info(
            "level={} requestId={} role={} method={} path={} status={} durationMs={}",
            level,
            call.attributes[RequestIdAttributeKey],
            PbRole.fromHeader(call.request.header("X-Role"))?.name ?: "ANON",
            call.request.httpMethod.value,
            call.request.path(),
            statusCode,
            durationMs,
        )
    }
}

private fun enforceRateLimit(
    call: io.ktor.server.application.ApplicationCall,
    config: PbShopConfig,
) {
    val path = call.request.path()
    if (
        path.startsWith(config.docsPath) ||
        path == "/" ||
        path == "/health" ||
        path == "${config.apiPrefix}/health" ||
        path == "${config.apiPrefix}/docs-status"
    ) {
        return
    }

    val limit =
        if (path.startsWith("${config.apiPrefix}/auth")) {
            config.rateLimit.authPerMinute
        } else {
            config.rateLimit.generalPerMinute
        }

    val clientKey =
        call.request.header("X-Client-Id")
            ?: call.request.header("X-Forwarded-For")
            ?: "local"

    val bucket = if (path.startsWith("${config.apiPrefix}/auth")) "auth" else "general"
    val key = "$clientKey:$bucket"
    val now = System.currentTimeMillis()
    val window =
        rateLimitWindows.compute(key) { _, current ->
            when {
                current == null || now - current.startedAtMillis >= 60_000L ->
                    RateLimitWindow(startedAtMillis = now, count = 1)
                else -> {
                    current.count += 1
                    current
                }
            }
        } ?: RateLimitWindow(startedAtMillis = now, count = 1)

    if (window.count > limit) {
        throw PbShopException(
            status = io.ktor.http.HttpStatusCode.TooManyRequests,
            code = "COMMON_004",
            message = "Rate limit exceeded for the current time window.",
        )
    }
}
