package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.api

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.server.application.ApplicationCall

data class EndpointDefinition(
    val method: HttpMethod,
    val path: String,
    val tag: String,
    val summary: String,
    val roles: Set<PbRole> = emptySet(),
    val handler: (ApplicationCall) -> StubResponse,
)
