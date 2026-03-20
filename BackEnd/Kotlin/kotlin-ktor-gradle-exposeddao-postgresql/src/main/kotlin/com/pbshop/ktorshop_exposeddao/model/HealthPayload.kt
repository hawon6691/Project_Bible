package com.pbshop.ktorshop_exposeddao.model

import kotlinx.serialization.Serializable

@Serializable
data class HealthPayload(
    val status: String,
    val app: String,
)

@Serializable
data class DocsStatusPayload(
    val openapiEnabled: Boolean,
    val swaggerEnabled: Boolean,
    val message: String,
)
