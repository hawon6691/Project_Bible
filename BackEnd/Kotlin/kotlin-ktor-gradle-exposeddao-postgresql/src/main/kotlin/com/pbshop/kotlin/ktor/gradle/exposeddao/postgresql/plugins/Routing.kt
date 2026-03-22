package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.plugins

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model.ApiEnvelope
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model.DocsStatusPayload
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model.HealthPayload
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting(config: PbShopConfig) {
    routing {
        get("/") {
            call.respond(
                HttpStatusCode.OK,
                ApiEnvelope(
                    success = true,
                    data = mapOf(
                        "service" to config.appName,
                        "apiPrefix" to config.apiPrefix,
                    ),
                ),
            )
        }

        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                ApiEnvelope(
                    success = true,
                    data = HealthPayload(
                        status = "UP",
                        app = config.appName,
                    ),
                ),
            )
        }

        route(config.apiPrefix) {
            get("/health") {
                call.respond(
                    HttpStatusCode.OK,
                    ApiEnvelope(
                        success = true,
                        data = HealthPayload(
                            status = "UP",
                            app = config.appName,
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
                            message = "OpenAPI and Swagger are planned for the next Kotlin milestone.",
                        ),
                    ),
                )
            }
        }
    }
}
