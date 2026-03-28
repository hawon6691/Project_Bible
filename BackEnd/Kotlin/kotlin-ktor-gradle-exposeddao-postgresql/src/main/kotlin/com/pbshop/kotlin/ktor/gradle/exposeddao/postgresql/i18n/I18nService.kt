package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.i18n

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class I18nService(
    private val repository: I18nRepository,
) {
    fun translations(
        locale: String?,
        namespace: String?,
        key: String?,
    ): StubResponse =
        StubResponse(
            data =
                repository.listTranslations(
                    locale = locale?.trim()?.takeIf { it.isNotBlank() },
                    namespace = namespace?.trim()?.takeIf { it.isNotBlank() },
                    key = key?.trim()?.takeIf { it.isNotBlank() },
                ).map(::translationPayload),
        )

    fun upsertTranslation(request: UpsertTranslationRequest): StubResponse {
        val locale = request.locale.trim().lowercase()
        val namespace = request.namespace.trim().lowercase()
        val key = request.key.trim()
        val value = request.value.trim()
        if (locale.length !in 2..10 || namespace.isBlank() || namespace.length > 100 || key.isBlank() || key.length > 191 || value.isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "번역 요청 값이 올바르지 않습니다.")
        }
        val saved =
            repository.upsertTranslation(
                NewTranslation(
                    locale = locale,
                    namespace = namespace,
                    key = key,
                    value = value,
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = translationPayload(saved))
    }

    fun deleteTranslation(id: Int): StubResponse {
        if (!repository.deleteTranslation(id)) {
            throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "번역을 찾을 수 없습니다.")
        }
        return StubResponse(data = mapOf("message" to "번역이 삭제되었습니다."))
    }

    fun exchangeRates(): StubResponse =
        StubResponse(
            data = repository.listExchangeRates().map(::exchangeRatePayload),
        )

    fun upsertExchangeRate(request: UpsertExchangeRateRequest): StubResponse {
        val baseCurrency = normalizeCurrency(request.baseCurrency)
        val targetCurrency = normalizeCurrency(request.targetCurrency)
        if (request.rate <= 0.0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "rate는 0보다 커야 합니다.")
        }
        val saved =
            repository.upsertExchangeRate(
                NewExchangeRate(
                    baseCurrency = baseCurrency,
                    targetCurrency = targetCurrency,
                    rate = request.rate,
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = exchangeRatePayload(saved))
    }

    fun convert(
        amount: Double,
        from: String,
        to: String,
    ): StubResponse {
        if (amount < 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "amount는 0 이상이어야 합니다.")
        }
        val originalCurrency = normalizeCurrency(from)
        val targetCurrency = normalizeCurrency(to)
        val rate = resolveRate(originalCurrency, targetCurrency)
        return StubResponse(
            data =
                mapOf(
                    "originalAmount" to amount,
                    "originalCurrency" to originalCurrency,
                    "convertedAmount" to String.format("%.2f", amount * rate).toDouble(),
                    "targetCurrency" to targetCurrency,
                    "rate" to rate,
                ),
        )
    }

    private fun resolveRate(
        from: String,
        to: String,
    ): Double {
        if (from == to) {
            return 1.0
        }
        repository.findExchangeRate(from, to)?.let { return it.rate }
        repository.findExchangeRate(to, from)?.let { return String.format("%.8f", 1 / it.rate).toDouble() }
        val fromToKrw = repository.findExchangeRate(from, "KRW") ?: repository.findExchangeRate("KRW", from)?.let { it.copy(rate = String.format("%.8f", 1 / it.rate).toDouble()) }
        val krwToTo = repository.findExchangeRate("KRW", to) ?: repository.findExchangeRate(to, "KRW")?.let { it.copy(rate = String.format("%.8f", 1 / it.rate).toDouble()) }
        if (fromToKrw != null && krwToTo != null) {
            return String.format("%.8f", fromToKrw.rate * krwToTo.rate).toDouble()
        }
        throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "환율 정보를 찾을 수 없습니다.")
    }

    private fun normalizeCurrency(value: String): String {
        val normalized = value.trim().uppercase()
        if (!normalized.matches(Regex("^[A-Z]{3,10}$"))) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "통화 코드는 3자 이상 10자 이하 영문자여야 합니다.")
        }
        return normalized
    }

    private fun translationPayload(record: TranslationRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "locale" to record.locale,
            "namespace" to record.namespace,
            "key" to record.key,
            "value" to record.value,
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun exchangeRatePayload(record: ExchangeRateRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "baseCurrency" to record.baseCurrency,
            "targetCurrency" to record.targetCurrency,
            "rate" to record.rate,
            "updatedAt" to record.updatedAt.toString(),
        )
}
