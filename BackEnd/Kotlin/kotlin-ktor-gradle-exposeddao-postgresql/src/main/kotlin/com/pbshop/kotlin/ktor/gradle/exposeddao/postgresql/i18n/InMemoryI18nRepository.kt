package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.i18n

import java.time.Instant

class InMemoryI18nRepository(
    translations: List<TranslationRecord>,
    exchangeRates: List<ExchangeRateRecord>,
) : I18nRepository {
    private val translations = linkedMapOf<Int, TranslationRecord>()
    private val exchangeRates = linkedMapOf<Int, ExchangeRateRecord>()
    private var translationSequence = 1
    private var exchangeRateSequence = 1

    init {
        translations.forEach {
            this.translations[it.id] = it
            translationSequence = maxOf(translationSequence, it.id + 1)
        }
        exchangeRates.forEach {
            this.exchangeRates[it.id] = it
            exchangeRateSequence = maxOf(exchangeRateSequence, it.id + 1)
        }
    }

    override fun listTranslations(
        locale: String?,
        namespace: String?,
        key: String?,
    ): List<TranslationRecord> =
        translations.values
            .filter { locale == null || it.locale == locale }
            .filter { namespace == null || it.namespace == namespace }
            .filter { key == null || it.key == key }
            .sortedBy { it.id }

    override fun upsertTranslation(newTranslation: NewTranslation): TranslationRecord {
        val existing =
            translations.values.firstOrNull {
                it.locale == newTranslation.locale &&
                    it.namespace == newTranslation.namespace &&
                    it.key == newTranslation.key
            }
        val now = Instant.now()
        val saved =
            if (existing == null) {
                TranslationRecord(
                    id = translationSequence++,
                    locale = newTranslation.locale,
                    namespace = newTranslation.namespace,
                    key = newTranslation.key,
                    value = newTranslation.value,
                    updatedAt = now,
                )
            } else {
                existing.copy(value = newTranslation.value, updatedAt = now)
            }
        translations[saved.id] = saved
        return saved
    }

    override fun deleteTranslation(id: Int): Boolean = translations.remove(id) != null

    override fun listExchangeRates(): List<ExchangeRateRecord> = exchangeRates.values.sortedByDescending { it.updatedAt }

    override fun upsertExchangeRate(newExchangeRate: NewExchangeRate): ExchangeRateRecord {
        val existing =
            exchangeRates.values.firstOrNull {
                it.baseCurrency == newExchangeRate.baseCurrency &&
                    it.targetCurrency == newExchangeRate.targetCurrency
            }
        val now = Instant.now()
        val saved =
            if (existing == null) {
                ExchangeRateRecord(
                    id = exchangeRateSequence++,
                    baseCurrency = newExchangeRate.baseCurrency,
                    targetCurrency = newExchangeRate.targetCurrency,
                    rate = newExchangeRate.rate,
                    updatedAt = now,
                )
            } else {
                existing.copy(rate = newExchangeRate.rate, updatedAt = now)
            }
        exchangeRates[saved.id] = saved
        return saved
    }

    override fun findExchangeRate(
        baseCurrency: String,
        targetCurrency: String,
    ): ExchangeRateRecord? =
        exchangeRates.values.firstOrNull { it.baseCurrency == baseCurrency && it.targetCurrency == targetCurrency }

    companion object {
        fun seeded(): InMemoryI18nRepository {
            val now = Instant.parse("2026-02-11T09:00:00Z")
            return InMemoryI18nRepository(
                translations =
                    listOf(
                        TranslationRecord(1, "en", "product", "product.lowest_price", "Lowest Price", now),
                        TranslationRecord(2, "en", "product", "product.add_to_cart", "Add to Cart", now),
                        TranslationRecord(3, "ko", "common", "common.checkout", "결제하기", now),
                    ),
                exchangeRates =
                    listOf(
                        ExchangeRateRecord(1, "KRW", "USD", 0.000748, now),
                        ExchangeRateRecord(2, "KRW", "JPY", 0.112, now),
                        ExchangeRateRecord(3, "USD", "EUR", 0.92, now),
                    ),
            )
        }
    }
}
