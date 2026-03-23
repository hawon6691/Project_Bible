package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.health

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbHealthService
import io.ktor.http.HttpStatusCode

class HealthService(
    private val config: PbShopConfig,
    private val dbHealthService: DbHealthService,
) {
    fun response(): Pair<HttpStatusCode, Map<String, Any?>> {
        val dbCheck = dbHealthService.check()
        val status = if (dbCheck.isUp) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable
        return status to
            mapOf(
                "status" to if (dbCheck.isUp) "UP" else "DOWN",
                "app" to config.appName,
                "baselineTrack" to config.baselineTrack,
                "checks" to
                    mapOf(
                        "db" to
                            mapOf(
                                "status" to dbCheck.status,
                                "engine" to dbCheck.engine,
                                "database" to dbCheck.database,
                                "message" to dbCheck.message,
                            ),
                    ),
            )
    }
}
