package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.i18n

import kotlinx.serialization.Serializable

@Serializable
data class UpsertTranslationRequest(
    val locale: String,
    val namespace: String,
    val key: String,
    val value: String,
)

@Serializable
data class UpsertExchangeRateRequest(
    val baseCurrency: String,
    val targetCurrency: String,
    val rate: Double,
)
