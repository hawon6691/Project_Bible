package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.platform

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.EndpointSpec
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig

class PlatformService(
    private val config: PbShopConfig,
    private val endpointSpecs: List<EndpointSpec>,
) {
    fun rootPayload(): Map<String, Any?> =
        mapOf(
            "service" to config.appName,
            "apiPrefix" to config.apiPrefix,
            "baselineTrack" to config.baselineTrack,
            "docsPath" to config.docsPath,
            "environment" to config.environment,
            "routeCount" to endpointSpecs.size,
        )
}
