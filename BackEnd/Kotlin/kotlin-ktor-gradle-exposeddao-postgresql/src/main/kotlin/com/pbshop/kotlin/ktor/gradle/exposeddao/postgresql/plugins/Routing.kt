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
                        "baselineTrack" to config.baselineTrack,
                        "docsPath" to config.docsPath,
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
                        baselineTrack = config.baselineTrack,
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
                        baselineTrack = config.baselineTrack,
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
