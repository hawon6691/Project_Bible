package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.api.EndpointDefinition
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.api.buildOpenApiDocument
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.api.buildSwaggerHtml
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.api.pbShopEndpoints
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondSuccess
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbHealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model.ComponentCheckPayload
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model.DbCheckPayload
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model.DocsStatusPayload
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model.HealthChecksPayload
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model.HealthPayload
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting(
    config: PbShopConfig,
    dbHealthService: DbHealthService,
) {
    val endpoints = pbShopEndpoints()
    val openApiDocument = buildOpenApiDocument(config, endpoints)
    val swaggerHtml = buildSwaggerHtml(config)

    routing {
        get("/") {
            call.respondSuccess(
                data =
                    mapOf(
                        "service" to config.appName,
                        "apiPrefix" to config.apiPrefix,
                        "baselineTrack" to config.baselineTrack,
                        "docsPath" to config.docsPath,
                        "environment" to config.environment,
                        "routeCount" to endpoints.size,
                    ),
            )
        }

        get("/health") {
            call.respondHealth(config, dbHealthService)
        }

        get("${config.docsPath}/openapi") {
            call.respondSuccess(data = openApiDocument)
        }

        get("${config.docsPath}/swagger") {
            call.respondText(
                text = swaggerHtml,
                contentType = ContentType.Text.Html,
                status = HttpStatusCode.OK,
            )
        }

        route(config.apiPrefix) {
            get("/health") {
                call.respondHealth(config, dbHealthService)
            }

            get("/docs-status") {
                call.respondSuccess(
                    data =
                        DocsStatusPayload(
                            openapiEnabled = true,
                            swaggerEnabled = true,
                            docsPath = config.docsPath,
                            message = "OpenAPI JSON and Swagger HTML are exposed on the aligned /docs path.",
                        ),
                )
            }

            endpoints.forEach { endpoint ->
                registerEndpoint(endpoint)
            }
        }
    }
}

private suspend fun ApplicationCall.respondHealth(
    config: PbShopConfig,
    dbHealthService: DbHealthService,
) {
    val dbCheck = dbHealthService.check()
    val httpStatus = if (dbCheck.isUp) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable

    respondSuccess(
        status = httpStatus,
        data =
            HealthPayload(
                status = if (dbCheck.isUp) "UP" else "DOWN",
                app = config.appName,
                baselineTrack = config.baselineTrack,
                checks =
                    HealthChecksPayload(
                        db =
                            DbCheckPayload(
                                status = dbCheck.status,
                                engine = dbCheck.engine,
                                database = dbCheck.database,
                                message = dbCheck.message,
                            ),
                        redis =
                            ComponentCheckPayload(
                                status = "NOT_CONFIGURED",
                                message = "Redis integration is stubbed in the Kotlin baseline track.",
                            ),
                        elasticsearch =
                            ComponentCheckPayload(
                                status = "NOT_CONFIGURED",
                                message = "Elasticsearch integration is stubbed in the Kotlin baseline track.",
                            ),
                    ),
            ),
    )
}

private fun Route.registerEndpoint(endpoint: EndpointDefinition) {
    when (endpoint.method) {
        HttpMethod.Get ->
            get(endpoint.path) {
                call.authorize(endpoint)
                call.respondStub(endpoint.handler(call))
            }
        HttpMethod.Post ->
            post(endpoint.path) {
                call.authorize(endpoint)
                call.respondStub(endpoint.handler(call))
            }
        HttpMethod.Patch ->
            patch(endpoint.path) {
                call.authorize(endpoint)
                call.respondStub(endpoint.handler(call))
            }
        HttpMethod.Delete ->
            delete(endpoint.path) {
                call.authorize(endpoint)
                call.respondStub(endpoint.handler(call))
            }
        else -> error("Unsupported method in endpoint catalog: ${endpoint.method.value}")
    }
}

private fun ApplicationCall.authorize(endpoint: EndpointDefinition) {
    if (endpoint.roles.isNotEmpty()) {
        requireAnyRole(*endpoint.roles.toTypedArray())
    }
}
