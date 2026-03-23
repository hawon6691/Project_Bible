package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.http.HttpMethod
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post

typealias EndpointExecutor = (String, EndpointRequest) -> EndpointResponse

fun Route.registerEndpointSpecs(
    specs: List<EndpointSpec>,
    executor: EndpointExecutor,
) {
    specs.forEach { spec ->
        when (spec.method) {
            HttpMethod.Get ->
                get(spec.path) {
                    call.authorize(spec.roles)
                    call.respondEndpoint(executor(spec.key, call.toEndpointRequest()))
                }
            HttpMethod.Post ->
                post(spec.path) {
                    call.authorize(spec.roles)
                    call.respondEndpoint(executor(spec.key, call.toEndpointRequest()))
                }
            HttpMethod.Patch ->
                patch(spec.path) {
                    call.authorize(spec.roles)
                    call.respondEndpoint(executor(spec.key, call.toEndpointRequest()))
                }
            HttpMethod.Delete ->
                delete(spec.path) {
                    call.authorize(spec.roles)
                    call.respondEndpoint(executor(spec.key, call.toEndpointRequest()))
                }
            else -> error("Unsupported method in endpoint spec: ${spec.method.value}")
        }
    }
}

private fun ApplicationCall.toEndpointRequest(): EndpointRequest =
    EndpointRequest(
        pathParams =
            parameters.names().associateWith { name ->
                parameters[name].orEmpty()
            },
        queryParams =
            request.queryParameters.names().associateWith { name ->
                request.queryParameters[name].orEmpty()
            },
        headers =
            request.headers.names().associateWith { name ->
                request.headers[name].orEmpty()
            },
    )

private fun ApplicationCall.authorize(roles: Set<PbRole>) {
    if (roles.isNotEmpty()) {
        requireAnyRole(*roles.toTypedArray())
    }
}
