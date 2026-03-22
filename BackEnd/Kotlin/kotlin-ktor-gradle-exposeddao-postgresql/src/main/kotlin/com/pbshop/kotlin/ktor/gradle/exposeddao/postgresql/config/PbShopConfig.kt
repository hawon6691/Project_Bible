package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config

import io.ktor.server.config.ApplicationConfig

data class PbShopConfig(
    val apiPrefix: String,
    val appName: String,
    val docsPath: String,
    val baselineTrack: String,
) {
    companion object {
        fun from(config: ApplicationConfig): PbShopConfig =
            PbShopConfig(
                apiPrefix = config.property("pbshop.apiPrefix").getString(),
                appName = config.property("pbshop.appName").getString(),
                docsPath = config.property("pbshop.docsPath").getString(),
                baselineTrack = config.property("pbshop.baselineTrack").getString(),
            )
    }
}
