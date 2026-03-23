package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common

data class EndpointRequest(
    val pathParams: Map<String, String> = emptyMap(),
    val queryParams: Map<String, String> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
) {
    fun pathParam(
        name: String,
        fallback: String,
    ): String = pathParams[name] ?: fallback
}
