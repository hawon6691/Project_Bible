package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

data class EndpointSpec(
    val method: HttpMethod,
    val path: String,
    val tag: String,
    val summary: String,
    val roles: Set<PbRole> = emptySet(),
    val successStatus: HttpStatusCode = HttpStatusCode.OK,
) {
    val key: String
        get() = "${method.value} $path"
}

data class StubOperation(
    val spec: EndpointSpec,
    val handler: (EndpointRequest) -> EndpointResponse,
)
