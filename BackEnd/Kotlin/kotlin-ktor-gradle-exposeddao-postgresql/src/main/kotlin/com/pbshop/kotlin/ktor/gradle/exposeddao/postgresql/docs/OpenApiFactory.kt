package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.docs

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.EndpointSpec
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.toJsonElement
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole

fun buildOpenApiDocument(
    config: PbShopConfig,
    endpoints: List<EndpointSpec>,
) =
    mapOf(
        "openapi" to "3.0.3",
        "info" to
            mapOf(
                "title" to "PBShop Kotlin Ktor Exposed DAO PostgreSQL API",
                "version" to "1.0.0",
                "description" to "Kotlin baseline PBShop contract generated from the aligned endpoint catalog.",
            ),
        "servers" to listOf(mapOf("url" to "http://127.0.0.1:8000")),
        "paths" to buildPaths(config, endpoints),
        "components" to
            mapOf(
                "securitySchemes" to
                    mapOf(
                        "roleHeader" to
                            mapOf(
                                "type" to "apiKey",
                                "in" to "header",
                                "name" to "X-Role",
                            ),
                    ),
            ),
        "x-baseline-track" to config.baselineTrack,
        "x-socket-events" to
            mapOf(
                "chat" to
                    listOf(
                        mapOf("event" to "joinRoom", "direction" to "client->server"),
                        mapOf("event" to "leaveRoom", "direction" to "client->server"),
                        mapOf("event" to "sendMessage", "direction" to "client->server"),
                        mapOf("event" to "newMessage", "direction" to "server->client"),
                        mapOf("event" to "messageRead", "direction" to "client->server"),
                        mapOf("event" to "readReceipt", "direction" to "server->client"),
                        mapOf("event" to "typing", "direction" to "client->server"),
                        mapOf("event" to "userTyping", "direction" to "server->client"),
                        mapOf("event" to "priceAlert", "direction" to "server->client"),
                    ),
            ),
    ).toJsonElement()

private fun buildPaths(
    config: PbShopConfig,
    endpoints: List<EndpointSpec>,
): Map<String, Any?> {
    val paths = linkedMapOf<String, MutableMap<String, Any?>>()

    fun addOperation(
        fullPath: String,
        method: String,
        tag: String,
        summary: String,
        roles: Set<PbRole>,
    ) {
        val operations = paths.getOrPut(fullPath) { linkedMapOf() }
        operations[method] =
            mapOf(
                "tags" to listOf(tag),
                "summary" to summary,
                "responses" to mapOf(methodResponseCode(method) to mapOf("description" to "PBShop envelope response")),
                "x-required-roles" to roles.map { it.name },
            )
    }

    addOperation("/health", "get", "Health", "System health check", emptySet())
    addOperation("${config.apiPrefix}/health", "get", "Health", "Versioned system health check", emptySet())
    addOperation("${config.apiPrefix}/docs-status", "get", "Docs", "OpenAPI and Swagger status", emptySet())
    addOperation("${config.docsPath}/openapi", "get", "Docs", "OpenAPI JSON export", emptySet())
    addOperation("${config.docsPath}/swagger", "get", "Docs", "Swagger HTML UI", emptySet())

    endpoints.forEach { endpoint ->
        addOperation(
            fullPath = "${config.apiPrefix}${endpoint.path}",
            method = endpoint.method.value.lowercase(),
            tag = endpoint.tag,
            summary = endpoint.summary,
            roles = endpoint.roles,
        )
    }

    return paths
}

private fun methodResponseCode(method: String): String =
    when (method) {
        "post" -> "201"
        else -> "200"
    }

fun buildSwaggerHtml(config: PbShopConfig): String =
    """
    <!DOCTYPE html>
    <html lang="en">
      <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>PBShop Kotlin Swagger</title>
        <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css" />
      </head>
      <body>
        <div id="swagger-ui"></div>
        <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
        <script>
          window.ui = SwaggerUIBundle({
            url: "${config.docsPath}/openapi",
            dom_id: "#swagger-ui",
            deepLinking: true,
            presets: [SwaggerUIBundle.presets.apis],
            layout: "BaseLayout"
          });
        </script>
      </body>
    </html>
    """.trimIndent()
