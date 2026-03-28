package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.i18n

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ExchangeRatesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.TranslationsTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ExposedDaoI18nRepository(
    private val databaseFactory: DatabaseFactory,
) : I18nRepository {
    override fun listTranslations(
        locale: String?,
        namespace: String?,
        key: String?,
    ): List<TranslationRecord> =
        databaseFactory.withTransaction {
            var predicate: Op<Boolean>? = null
            locale?.let { value -> predicate = (predicate ?: Op.TRUE) and (TranslationsTable.locale eq value) }
            namespace?.let { value -> predicate = (predicate ?: Op.TRUE) and (TranslationsTable.namespace eq value) }
            key?.let { value -> predicate = (predicate ?: Op.TRUE) and (TranslationsTable.translationKey eq value) }
            val query = TranslationsTable.selectAll()
            if (predicate != null) {
                query.where { predicate!! }
            }
            query.orderBy(TranslationsTable.id to SortOrder.ASC)
                .map {
                    TranslationRecord(
                        id = it[TranslationsTable.id].value,
                        locale = it[TranslationsTable.locale],
                        namespace = it[TranslationsTable.namespace],
                        key = it[TranslationsTable.translationKey],
                        value = it[TranslationsTable.translationValue],
                        updatedAt = it[TranslationsTable.updatedAt],
                    )
                }
        }

    override fun upsertTranslation(newTranslation: NewTranslation): TranslationRecord =
        databaseFactory.withTransaction {
            val existing =
                TranslationsTable.selectAll()
                    .where {
                        (TranslationsTable.locale eq newTranslation.locale) and
                            (TranslationsTable.namespace eq newTranslation.namespace) and
                            (TranslationsTable.translationKey eq newTranslation.key)
                    }.singleOrNull()
            val now = Instant.now()
            if (existing == null) {
                val insertedId =
                    TranslationsTable.insert {
                        it[locale] = newTranslation.locale
                        it[namespace] = newTranslation.namespace
                        it[translationKey] = newTranslation.key
                        it[translationValue] = newTranslation.value
                        it[createdAt] = now
                        it[updatedAt] = now
                    } get TranslationsTable.id
                TranslationRecord(insertedId.value, newTranslation.locale, newTranslation.namespace, newTranslation.key, newTranslation.value, now)
            } else {
                val id = existing[TranslationsTable.id].value
                TranslationsTable.update({ TranslationsTable.id eq id }) {
                    it[translationValue] = newTranslation.value
                    it[updatedAt] = now
                }
                TranslationRecord(id, newTranslation.locale, newTranslation.namespace, newTranslation.key, newTranslation.value, now)
            }
        }

    override fun deleteTranslation(id: Int): Boolean =
        databaseFactory.withTransaction {
            TranslationsTable.deleteWhere { TranslationsTable.id eq id } > 0
        }

    override fun listExchangeRates(): List<ExchangeRateRecord> =
        databaseFactory.withTransaction {
            ExchangeRatesTable.selectAll()
                .orderBy(ExchangeRatesTable.updatedAt to SortOrder.DESC)
                .map {
                    ExchangeRateRecord(
                        id = it[ExchangeRatesTable.id].value,
                        baseCurrency = it[ExchangeRatesTable.baseCurrency],
                        targetCurrency = it[ExchangeRatesTable.targetCurrency],
                        rate = it[ExchangeRatesTable.rate].toDouble(),
                        updatedAt = it[ExchangeRatesTable.updatedAtExchange] ?: it[ExchangeRatesTable.updatedAt],
                    )
                }
        }

    override fun upsertExchangeRate(newExchangeRate: NewExchangeRate): ExchangeRateRecord =
        databaseFactory.withTransaction {
            val existing =
                ExchangeRatesTable.selectAll()
                    .where {
                        (ExchangeRatesTable.baseCurrency eq newExchangeRate.baseCurrency) and
                            (ExchangeRatesTable.targetCurrency eq newExchangeRate.targetCurrency)
                    }.singleOrNull()
            val now = Instant.now()
            if (existing == null) {
                val insertedId =
                    ExchangeRatesTable.insert {
                        it[baseCurrency] = newExchangeRate.baseCurrency
                        it[targetCurrency] = newExchangeRate.targetCurrency
                        it[rate] = newExchangeRate.rate.toBigDecimal()
                        it[updatedAtExchange] = now
                        it[createdAt] = now
                        it[updatedAt] = now
                    } get ExchangeRatesTable.id
                ExchangeRateRecord(insertedId.value, newExchangeRate.baseCurrency, newExchangeRate.targetCurrency, newExchangeRate.rate, now)
            } else {
                val id = existing[ExchangeRatesTable.id].value
                ExchangeRatesTable.update({ ExchangeRatesTable.id eq id }) {
                    it[rate] = newExchangeRate.rate.toBigDecimal()
                    it[updatedAtExchange] = now
                    it[updatedAt] = now
                }
                ExchangeRateRecord(id, newExchangeRate.baseCurrency, newExchangeRate.targetCurrency, newExchangeRate.rate, now)
            }
        }

    override fun findExchangeRate(
        baseCurrency: String,
        targetCurrency: String,
    ): ExchangeRateRecord? =
        databaseFactory.withTransaction {
            ExchangeRatesTable.selectAll()
                .where {
                    (ExchangeRatesTable.baseCurrency eq baseCurrency) and
                        (ExchangeRatesTable.targetCurrency eq targetCurrency)
                }.singleOrNull()
                ?.let {
                    ExchangeRateRecord(
                        id = it[ExchangeRatesTable.id].value,
                        baseCurrency = it[ExchangeRatesTable.baseCurrency],
                        targetCurrency = it[ExchangeRatesTable.targetCurrency],
                        rate = it[ExchangeRatesTable.rate].toDouble(),
                        updatedAt = it[ExchangeRatesTable.updatedAtExchange] ?: it[ExchangeRatesTable.updatedAt],
                    )
                }
        }
}
