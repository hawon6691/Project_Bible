package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put

class SpecController(
    private val service: SpecService,
) {
    fun Route.register() {
        get("/specs/definitions") {
            call.respondStub(service.listDefinitions(call.request.queryParameters["categoryId"]?.toIntOrNull()))
        }
        post("/specs/definitions") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.createDefinition(call.receive()))
        }
        patch("/specs/definitions/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.updateDefinition(call.definitionId(), call.receive()))
        }
        delete("/specs/definitions/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.deleteDefinition(call.definitionId()))
        }
        get("/products/{id}/specs") {
            call.respondStub(service.listProductSpecs(call.productId()))
        }
        put("/products/{id}/specs") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.replaceProductSpecs(call.productId(), call.receive()))
        }
        post("/specs/compare") {
            call.respondStub(service.compare(call.receive()))
        }
        post("/specs/compare/scored") {
            call.respondStub(service.compareScored(call.receive()))
        }
        put("/specs/scores/{specDefId}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.replaceSpecScores(call.specDefinitionId(), call.receive()))
        }
    }

    private fun io.ktor.server.application.ApplicationCall.definitionId(): Int = parameters["id"]?.toIntOrNull() ?: 0

    private fun io.ktor.server.application.ApplicationCall.productId(): Int = parameters["id"]?.toIntOrNull() ?: 0

    private fun io.ktor.server.application.ApplicationCall.specDefinitionId(): Int = parameters["specDefId"]?.toIntOrNull() ?: 0
}
