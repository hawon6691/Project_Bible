package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.docs

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.EndpointSpec
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.config.PbShopConfig

class DocsService(
    private val config: PbShopConfig,
    private val endpointSpecs: List<EndpointSpec>,
) {
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
