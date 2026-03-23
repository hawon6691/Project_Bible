package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.i18n

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun i18nOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/i18n/translations", "I18n", "Translations") {
            StubResponse(data = listOf(mapOf("id" to 1, "key" to "product.lowest_price", "value" to "Lowest Price", "locale" to "en")))
        },
        endpoint(HttpMethod.Post, "/admin/i18n/translations", "I18n", "Upsert translation", roles = setOf(PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "key" to "product.add_to_cart", "locale" to "en"))
        },
        endpoint(HttpMethod.Delete, "/admin/i18n/translations/{id}", "I18n", "Delete translation", roles = setOf(PbRole.ADMIN)) {
            message("Translation deleted.")
        },
        endpoint(HttpMethod.Get, "/i18n/exchange-rates", "I18n", "Exchange rates") {
            StubResponse(data = listOf(mapOf("baseCurrency" to "KRW", "targetCurrency" to "USD", "rate" to 0.000748)))
        },
        endpoint(HttpMethod.Get, "/i18n/convert", "I18n", "Currency conversion") {
            StubResponse(data = mapOf("originalAmount" to 1590000, "originalCurrency" to "KRW", "convertedAmount" to 1189.32, "targetCurrency" to "USD"))
        },
    )
