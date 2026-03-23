package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model

import kotlinx.serialization.Serializable

@Serializable
data class HealthPayload(
    val status: String,
    val app: String,
    val baselineTrack: String,
    val checks: HealthChecksPayload,
)

@Serializable
data class DocsStatusPayload(
    val openapiEnabled: Boolean,
    val swaggerEnabled: Boolean,
    val docsPath: String,
    val message: String,
)

@Serializable
data class HealthChecksPayload(
    val db: DbCheckPayload,
    val redis: ComponentCheckPayload,
    val elasticsearch: ComponentCheckPayload,
)

@Serializable
data class DbCheckPayload(
    val status: String,
    val engine: String,
    val database: String,
    val message: String? = null,
)

@Serializable
data class ComponentCheckPayload(
    val status: String,
    val message: String? = null,
)
