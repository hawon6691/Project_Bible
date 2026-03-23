package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.platform

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.EndpointSpec
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondSuccess
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbHealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.docs.buildOpenApiDocument
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.docs.buildSwaggerHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

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

    fun docsStatusPayload(): Map<String, Any?> =
        mapOf(
            "openapiEnabled" to true,
            "swaggerEnabled" to true,
            "docsPath" to config.docsPath,
            "message" to "OpenAPI JSON and Swagger HTML are exposed on the aligned /docs path.",
        )

    fun openApiDocument() = buildOpenApiDocument(config, endpointSpecs)

    fun swaggerHtml(): String = buildSwaggerHtml(config)
}

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
                        "redis" to
                            mapOf(
                                "status" to "NOT_CONFIGURED",
                                "message" to "Redis integration is stubbed in the Kotlin baseline track.",
                            ),
                        "elasticsearch" to
                            mapOf(
                                "status" to "NOT_CONFIGURED",
                                "message" to "Elasticsearch integration is stubbed in the Kotlin baseline track.",
                            ),
                    ),
            )
    }
}

class PlatformController(
    private val platformService: PlatformService,
    private val healthService: HealthService,
) {
    fun Route.registerPublicRoutes() {
        get("/") {
            call.respondSuccess(data = platformService.rootPayload())
        }

        get("/health") {
            val (status, payload) = healthService.response()
            call.respondSuccess(status = status, data = payload)
        }

        get("/docs/openapi") {
            call.respondSuccess(data = platformService.openApiDocument())
        }

        get("/docs/swagger") {
            call.respondText(
                text = platformService.swaggerHtml(),
                contentType = ContentType.Text.Html,
                status = HttpStatusCode.OK,
            )
        }
    }

    fun Route.registerApiRoutes() {
        get("/health") {
            val (status, payload) = healthService.response()
            call.respondSuccess(status = status, data = payload)
        }

        get("/docs-status") {
            call.respondSuccess(data = platformService.docsStatusPayload())
        }
    }
}
