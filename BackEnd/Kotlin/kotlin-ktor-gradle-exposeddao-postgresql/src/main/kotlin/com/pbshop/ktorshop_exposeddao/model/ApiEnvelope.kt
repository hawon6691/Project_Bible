package com.pbshop.ktorshop_exposeddao.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiEnvelope<T>(
    val success: Boolean,
    val data: T? = null,
    val meta: JsonElement? = null,
    val error: ApiErrorPayload? = null,
)

@Serializable
data class ApiErrorPayload(
    val code: String,
    val message: String,
)
