package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.docs

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondSuccess
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

class DocsController(
    private val docsService: DocsService,
) {
    fun Route.registerPublicRoutes() {
        get("/docs/openapi") {
            call.respondSuccess(data = docsService.openApiDocument())
        }

        get("/docs/swagger") {
            call.respondText(
                text = docsService.swaggerHtml(),
                contentType = ContentType.Text.Html,
                status = HttpStatusCode.OK,
            )
        }
    }

    fun Route.registerApiRoutes() {
        get("/docs-status") {
            call.respondSuccess(data = docsService.docsStatusPayload())
        }
    }
}
