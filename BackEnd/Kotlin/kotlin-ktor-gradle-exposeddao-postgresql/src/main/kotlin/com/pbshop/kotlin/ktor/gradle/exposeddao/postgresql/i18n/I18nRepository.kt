package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.i18n

import java.time.Instant

data class TranslationRecord(
    val id: Int,
    val locale: String,
    val namespace: String,
    val key: String,
    val value: String,
    val updatedAt: Instant,
)

data class ExchangeRateRecord(
    val id: Int,
    val baseCurrency: String,
    val targetCurrency: String,
    val rate: Double,
    val updatedAt: Instant,
)

data class NewTranslation(
    val locale: String,
    val namespace: String,
    val key: String,
    val value: String,
)

data class NewExchangeRate(
    val baseCurrency: String,
    val targetCurrency: String,
    val rate: Double,
)

interface I18nRepository {
    fun listTranslations(
        locale: String?,
        namespace: String?,
        key: String?,
    ): List<TranslationRecord>

    fun upsertTranslation(newTranslation: NewTranslation): TranslationRecord

    fun deleteTranslation(id: Int): Boolean

    fun listExchangeRates(): List<ExchangeRateRecord>

    fun upsertExchangeRate(newExchangeRate: NewExchangeRate): ExchangeRateRecord

    fun findExchangeRate(
        baseCurrency: String,
        targetCurrency: String,
    ): ExchangeRateRecord?
}
