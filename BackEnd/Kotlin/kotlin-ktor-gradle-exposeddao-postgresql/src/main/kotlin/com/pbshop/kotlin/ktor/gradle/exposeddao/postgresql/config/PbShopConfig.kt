package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config

import io.ktor.server.config.ApplicationConfig

data class PbShopConfig(
    val apiPrefix: String,
    val appName: String,
) {
    companion object {
        fun from(config: ApplicationConfig): PbShopConfig =
            PbShopConfig(
                apiPrefix = config.property("pbshop.apiPrefix").getString(),
                appName = config.property("pbshop.appName").getString(),
            )
    }
}
