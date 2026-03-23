package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config

import io.ktor.server.config.ApplicationConfig

data class PbShopConfig(
    val apiPrefix: String,
    val appName: String,
    val docsPath: String,
    val baselineTrack: String,
    val environment: String,
    val rateLimit: RateLimitConfig,
    val security: SecurityConfig,
    val observability: ObservabilityConfig,
    val database: DatabaseConfig,
) {
    companion object {
        fun from(config: ApplicationConfig): PbShopConfig =
            PbShopConfig(
                apiPrefix = config.property("pbshop.apiPrefix").getString(),
                appName = config.property("pbshop.appName").getString(),
                docsPath = config.property("pbshop.docsPath").getString(),
                baselineTrack = config.property("pbshop.baselineTrack").getString(),
                environment = config.property("pbshop.environment").getString(),
                rateLimit = RateLimitConfig.from(config.config("pbshop.rateLimit")),
                security = SecurityConfig.from(config.config("pbshop.security")),
                observability = ObservabilityConfig.from(config.config("pbshop.observability")),
                database = DatabaseConfig.from(config.config("pbshop.database")),
            )
    }
}

data class RateLimitConfig(
    val generalPerMinute: Int,
    val authPerMinute: Int,
) {
    companion object {
        fun from(config: ApplicationConfig): RateLimitConfig =
            RateLimitConfig(
                generalPerMinute = config.property("generalPerMinute").getString().toInt(),
                authPerMinute = config.property("authPerMinute").getString().toInt(),
            )
    }
}

data class SecurityConfig(
    val requestIdHeader: String,
) {
    companion object {
        fun from(config: ApplicationConfig): SecurityConfig =
            SecurityConfig(
                requestIdHeader = config.property("requestIdHeader").getString(),
            )
    }
}

data class ObservabilityConfig(
    val traceBufferLimit: Int,
) {
    companion object {
        fun from(config: ApplicationConfig): ObservabilityConfig =
            ObservabilityConfig(
                traceBufferLimit = config.property("traceBufferLimit").getString().toInt(),
            )
    }
}

data class DatabaseConfig(
    val url: String,
    val username: String,
    val password: String,
    val driver: String,
    val engine: String,
    val database: String,
    val maxPoolSize: Int,
    val minIdle: Int,
) {
    companion object {
        fun from(config: ApplicationConfig): DatabaseConfig =
            DatabaseConfig(
                url = config.property("url").getString(),
                username = config.property("username").getString(),
                password = config.property("password").getString(),
                driver = config.property("driver").getString(),
                engine = config.property("engine").getString(),
                database = config.property("database").getString(),
                maxPoolSize = config.property("maxPoolSize").getString().toInt(),
                minIdle = config.property("minIdle").getString().toInt(),
            )
    }
}
