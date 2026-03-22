package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model

import kotlinx.serialization.Serializable

@Serializable
data class HealthPayload(
    val status: String,
    val app: String,
    val baselineTrack: String,
)

@Serializable
data class DocsStatusPayload(
    val openapiEnabled: Boolean,
    val swaggerEnabled: Boolean,
    val docsPath: String,
    val message: String,
)
