package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.auto

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.AutoOptionsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.AutoTrimsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CarModelsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.LeaseOffersTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class ExposedDaoAutoRepository(
    private val databaseFactory: DatabaseFactory,
) : AutoRepository {
    override fun listModels(brand: String?, type: String?): List<AutoModelRecord> =
        databaseFactory.withTransaction {
            CarModelsTable.selectAll()
                .where {
                    var predicate = CarModelsTable.isActive eq true
                    if (!brand.isNullOrBlank()) predicate = predicate and (CarModelsTable.brand eq brand)
                    if (!type.isNullOrBlank()) predicate = predicate and (CarModelsTable.type eq type.uppercase())
                    predicate
                }.map(::toModel)
        }

    override fun findModelById(modelId: Int): AutoModelRecord? =
        databaseFactory.withTransaction {
            CarModelsTable.selectAll()
                .where { (CarModelsTable.id eq modelId) and (CarModelsTable.isActive eq true) }
                .singleOrNull()
                ?.let(::toModel)
        }

    override fun listTrims(modelId: Int): List<AutoTrimRecord> =
        databaseFactory.withTransaction {
            AutoTrimsTable.selectAll().where { AutoTrimsTable.carModel eq modelId }.map {
                AutoTrimRecord(it[AutoTrimsTable.id].value, it[AutoTrimsTable.carModel].value, it[AutoTrimsTable.name], it[AutoTrimsTable.basePrice])
            }
        }

    override fun listOptionsForTrimIds(trimIds: List<Int>): List<AutoOptionRecord> {
        if (trimIds.isEmpty()) return emptyList()
        return databaseFactory.withTransaction {
            AutoOptionsTable.selectAll().where { AutoOptionsTable.trim inList trimIds }.map {
                AutoOptionRecord(it[AutoOptionsTable.id].value, it[AutoOptionsTable.trim].value, it[AutoOptionsTable.name], it[AutoOptionsTable.price])
            }
        }
    }

    override fun listLeaseOffers(modelId: Int): List<LeaseOfferRecord> =
        databaseFactory.withTransaction {
            LeaseOffersTable.selectAll().where { LeaseOffersTable.carModel eq modelId }.map {
                LeaseOfferRecord(
                    id = it[LeaseOffersTable.id].value,
                    modelId = it[LeaseOffersTable.carModel].value,
                    company = it[LeaseOffersTable.company],
                    type = it[LeaseOffersTable.type],
                    monthlyPayment = it[LeaseOffersTable.monthlyPayment],
                    deposit = it[LeaseOffersTable.deposit],
                    contractMonths = it[LeaseOffersTable.contractMonths],
                    annualMileage = it[LeaseOffersTable.annualMileage],
                    isActive = it[LeaseOffersTable.isActive],
                )
            }
        }

    private fun toModel(row: ResultRow): AutoModelRecord =
        AutoModelRecord(
            id = row[CarModelsTable.id].value,
            brand = row[CarModelsTable.brand],
            name = row[CarModelsTable.name],
            type = row[CarModelsTable.type],
            year = row[CarModelsTable.year],
            basePrice = row[CarModelsTable.basePrice],
            imageUrl = row[CarModelsTable.imageUrl],
            isActive = row[CarModelsTable.isActive],
        )
}
