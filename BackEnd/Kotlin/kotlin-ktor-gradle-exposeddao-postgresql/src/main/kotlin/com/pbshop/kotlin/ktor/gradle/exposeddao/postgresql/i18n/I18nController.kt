package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.i18n

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post

class I18nController(
    private val service: I18nService,
) {
    fun Route.register() {
        get("/i18n/translations") {
            call.respondStub(
                service.translations(
                    locale = call.request.queryParameters["locale"],
                    namespace = call.request.queryParameters["namespace"],
                    key = call.request.queryParameters["key"],
                ),
            )
        }
        post("/admin/i18n/translations") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.upsertTranslation(call.receive()))
        }
        delete("/admin/i18n/translations/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.deleteTranslation(call.parameters["id"]?.toIntOrNull() ?: 0))
        }
        get("/i18n/exchange-rates") {
            call.respondStub(service.exchangeRates())
        }
        post("/admin/i18n/exchange-rates") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.upsertExchangeRate(call.receive()))
        }
        get("/i18n/convert") {
            call.respondStub(
                service.convert(
                    amount = call.request.queryParameters["amount"]?.toDoubleOrNull() ?: 0.0,
                    from = call.request.queryParameters["from"] ?: "",
                    to = call.request.queryParameters["to"] ?: "",
                ),
            )
        }
    }
}
