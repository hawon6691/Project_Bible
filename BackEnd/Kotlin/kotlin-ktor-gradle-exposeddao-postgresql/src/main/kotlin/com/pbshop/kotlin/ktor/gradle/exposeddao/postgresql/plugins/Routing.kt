package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DbHealthService
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model.ApiEnvelope
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model.DbCheckPayload
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model.DocsStatusPayload
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model.HealthChecksPayload
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model.HealthPayload
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting(
    config: PbShopConfig,
    dbHealthService: DbHealthService,
) {
    routing {
        get("/") {
            call.respond(
                HttpStatusCode.OK,
                ApiEnvelope(
                    success = true,
                    data = mapOf(
                        "service" to config.appName,
                        "apiPrefix" to config.apiPrefix,
                        "baselineTrack" to config.baselineTrack,
                        "docsPath" to config.docsPath,
                    ),
                ),
            )
        }

        get("/health") {
            val dbCheck = dbHealthService.check()
            val httpStatus = if (dbCheck.isUp) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable

            call.respond(
                httpStatus,
                ApiEnvelope(
                    success = true,
                    data = HealthPayload(
                        status = if (dbCheck.isUp) "UP" else "DOWN",
                        app = config.appName,
                        baselineTrack = config.baselineTrack,
                        checks = HealthChecksPayload(
                            db = DbCheckPayload(
                                status = dbCheck.status,
                                engine = dbCheck.engine,
                                database = dbCheck.database,
                                message = dbCheck.message,
                            ),
                        ),
                    ),
                ),
            )
        }

        route(config.apiPrefix) {
            get("/health") {
                val dbCheck = dbHealthService.check()
                val httpStatus = if (dbCheck.isUp) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable

                call.respond(
                    httpStatus,
                    ApiEnvelope(
                        success = true,
                        data = HealthPayload(
                            status = if (dbCheck.isUp) "UP" else "DOWN",
                            app = config.appName,
                            baselineTrack = config.baselineTrack,
                            checks = HealthChecksPayload(
                                db = DbCheckPayload(
                                    status = dbCheck.status,
                                    engine = dbCheck.engine,
                                    database = dbCheck.database,
                                    message = dbCheck.message,
                                ),
                            ),
                        ),
                    ),
                )
            }

            get("/docs-status") {
                call.respond(
                    HttpStatusCode.OK,
                    ApiEnvelope(
                    success = true,
                    data = DocsStatusPayload(
                        openapiEnabled = false,
                        swaggerEnabled = false,
                        docsPath = config.docsPath,
                        message = "OpenAPI and Swagger will be exposed at the aligned /docs path in a later Kotlin milestone.",
                    ),
                    ),
                )
            }
        }
    }
}
