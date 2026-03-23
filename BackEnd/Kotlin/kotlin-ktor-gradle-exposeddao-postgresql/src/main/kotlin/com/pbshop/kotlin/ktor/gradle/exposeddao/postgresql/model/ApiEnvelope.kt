package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiEnvelope<T>(
    val success: Boolean,
    val data: T? = null,
    val meta: JsonElement? = null,
    val error: ApiErrorPayload? = null,
    val requestId: String,
    val timestamp: String,
    val errorCode: String? = error?.code,
    val message: String? = error?.message,
)

@Serializable
data class ApiErrorPayload(
    val code: String,
    val message: String,
    val path: String? = null,
)
