package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config

import io.ktor.server.config.ApplicationConfig

data class PbShopConfig(
    val apiPrefix: String,
    val appName: String,
    val docsPath: String,
    val baselineTrack: String,
    val database: DatabaseConfig,
) {
    companion object {
        fun from(config: ApplicationConfig): PbShopConfig =
            PbShopConfig(
                apiPrefix = config.property("pbshop.apiPrefix").getString(),
                appName = config.property("pbshop.appName").getString(),
                docsPath = config.property("pbshop.docsPath").getString(),
                baselineTrack = config.property("pbshop.baselineTrack").getString(),
                database = DatabaseConfig.from(config.config("pbshop.database")),
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
