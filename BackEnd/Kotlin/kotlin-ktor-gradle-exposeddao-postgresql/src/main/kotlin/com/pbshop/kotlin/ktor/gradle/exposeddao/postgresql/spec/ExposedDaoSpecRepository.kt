package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.spec

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CategoriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductSpecsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SpecDefinitionsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SpecScoresTable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class ExposedDaoSpecRepository(
    private val databaseFactory: DatabaseFactory,
) : SpecRepository {
    override fun listDefinitions(categoryId: Int?): List<SpecDefinitionRecord> =
        databaseFactory.withTransaction {
            SpecDefinitionsTable
                .innerJoin(CategoriesTable)
                .selectAll()
                .apply {
                    if (categoryId != null) {
                        where { SpecDefinitionsTable.category eq categoryId }
                    }
                }.orderBy(SpecDefinitionsTable.category to SortOrder.ASC, SpecDefinitionsTable.sortOrder to SortOrder.ASC, SpecDefinitionsTable.id to SortOrder.ASC)
                .map(::toDefinitionRecord)
        }

    override fun findDefinitionById(id: Int): SpecDefinitionRecord? =
        databaseFactory.withTransaction {
            SpecDefinitionsTable
                .innerJoin(CategoriesTable)
                .selectAll()
                .where { SpecDefinitionsTable.id eq id }
                .limit(1)
                .firstOrNull()
                ?.let(::toDefinitionRecord)
        }

    override fun createDefinition(newDefinition: NewSpecDefinition): SpecDefinitionRecord =
        databaseFactory.withTransaction {
            val id =
                SpecDefinitionsTable.insertAndGetId {
                    it[category] = newDefinition.categoryId
                    it[name] = newDefinition.name
                    it[type] = newDefinition.type
                    it[options] = newDefinition.options?.let(::encodeJsonList)
                    it[unit] = newDefinition.unit
                    it[isComparable] = newDefinition.isComparable
                    it[dataType] = newDefinition.dataType
                    it[sortOrder] = newDefinition.sortOrder
                }.value
            requireNotNull(findDefinitionById(id))
        }

    override fun updateDefinition(
        id: Int,
        update: SpecDefinitionUpdate,
    ): SpecDefinitionRecord =
        databaseFactory.withTransaction {
            val current = requireNotNull(findDefinitionById(id)) { "Spec definition $id not found" }
            SpecDefinitionsTable.update({ SpecDefinitionsTable.id eq id }) {
                it[category] = update.categoryId ?: current.categoryId
                it[name] = update.name ?: current.name
                it[type] = update.type ?: current.type
                it[options] = encodeJsonList(update.options ?: current.options)
                it[unit] = update.unit ?: current.unit
                it[isComparable] = update.isComparable ?: current.isComparable
                it[dataType] = update.dataType ?: current.dataType
                it[sortOrder] = update.sortOrder ?: current.sortOrder
            }
            requireNotNull(findDefinitionById(id))
        }

    override fun deleteDefinition(id: Int) {
        databaseFactory.withTransaction {
            SpecScoresTable.deleteWhere { specDefinition eq id }
            SpecDefinitionsTable.deleteWhere { SpecDefinitionsTable.id eq id }
        }
    }

    override fun definitionInUse(id: Int): Boolean =
        databaseFactory.withTransaction {
            !ProductSpecsTable.selectAll().where { ProductSpecsTable.specDefinition eq id }.limit(1).empty()
        }

    override fun categoryExists(categoryId: Int): Boolean =
        databaseFactory.withTransaction {
            !CategoriesTable.selectAll().where { CategoriesTable.id eq categoryId }.limit(1).empty()
        }

    override fun productExists(productId: Int): Boolean =
        databaseFactory.withTransaction {
            !ProductsTable.selectAll().where { (ProductsTable.id eq productId) and ProductsTable.deletedAt.isNull() }.limit(1).empty()
        }

    override fun listProductSpecs(productId: Int): List<ProductSpecRecord> =
        databaseFactory.withTransaction {
            ProductSpecsTable
                .innerJoin(SpecDefinitionsTable)
                .selectAll()
                .where { ProductSpecsTable.product eq productId }
                .orderBy(SpecDefinitionsTable.sortOrder to SortOrder.ASC, SpecDefinitionsTable.id to SortOrder.ASC)
                .map(::toProductSpecRecord)
        }

    override fun replaceProductSpecs(
        productId: Int,
        values: List<NewProductSpecValue>,
    ): List<ProductSpecRecord> =
        databaseFactory.withTransaction {
            ProductSpecsTable.deleteWhere { ProductSpecsTable.product eq productId }
            values.forEach { value ->
                ProductSpecsTable.insertAndGetId {
                    it[product] = productId
                    it[specDefinition] = value.specDefinitionId
                    it[ProductSpecsTable.value] = value.value
                    it[numericValue] = value.numericValue?.toBigDecimal()
                }
            }
            listProductSpecs(productId)
        }

    override fun listComparedProducts(productIds: List<Int>): List<ComparedProductRecord> =
        databaseFactory.withTransaction {
            val products =
                ProductsTable
                    .selectAll()
                    .where { (ProductsTable.id inList productIds) and ProductsTable.deletedAt.isNull() }
                    .associate { it[ProductsTable.id].value to it[ProductsTable.name] }

            productIds.mapNotNull { productId ->
                products[productId]?.let { name ->
                    ComparedProductRecord(
                        productId = productId,
                        productName = name,
                        specs = listProductSpecs(productId),
                    )
                }
            }
        }

    override fun listSpecScores(specDefinitionId: Int): List<SpecScoreRecord> =
        databaseFactory.withTransaction {
            SpecScoresTable
                .selectAll()
                .where { SpecScoresTable.specDefinition eq specDefinitionId }
                .orderBy(SpecScoresTable.id to SortOrder.ASC)
                .map(::toScoreRecord)
        }

    override fun replaceSpecScores(
        specDefinitionId: Int,
        values: List<NewSpecScore>,
    ): List<SpecScoreRecord> =
        databaseFactory.withTransaction {
            SpecScoresTable.deleteWhere { specDefinition eq specDefinitionId }
            values.forEach { value ->
                SpecScoresTable.insertAndGetId {
                    it[specDefinition] = specDefinitionId
                    it[SpecScoresTable.value] = value.value
                    it[score] = value.score
                    it[benchmarkSource] = value.benchmarkSource
                }
            }
            listSpecScores(specDefinitionId)
        }

    private fun toDefinitionRecord(row: ResultRow): SpecDefinitionRecord =
        SpecDefinitionRecord(
            id = row[SpecDefinitionsTable.id].value,
            categoryId = row[SpecDefinitionsTable.category].value,
            categoryName = row[CategoriesTable.name],
            name = row[SpecDefinitionsTable.name],
            type = row[SpecDefinitionsTable.type],
            options = decodeJsonList(row[SpecDefinitionsTable.options]),
            unit = row[SpecDefinitionsTable.unit],
            isComparable = row[SpecDefinitionsTable.isComparable],
            dataType = row[SpecDefinitionsTable.dataType],
            sortOrder = row[SpecDefinitionsTable.sortOrder],
        )

    private fun toProductSpecRecord(row: ResultRow): ProductSpecRecord =
        ProductSpecRecord(
            specDefinitionId = row[ProductSpecsTable.specDefinition].value,
            name = row[SpecDefinitionsTable.name],
            value = row[ProductSpecsTable.value],
            numericValue = row[ProductSpecsTable.numericValue]?.toDouble(),
        )

    private fun toScoreRecord(row: ResultRow): SpecScoreRecord =
        SpecScoreRecord(
            id = row[SpecScoresTable.id].value,
            specDefinitionId = row[SpecScoresTable.specDefinition].value,
            value = row[SpecScoresTable.value],
            score = row[SpecScoresTable.score],
            benchmarkSource = row[SpecScoresTable.benchmarkSource],
        )

    private fun encodeJsonList(values: List<String>?): String? =
        values?.let { Json.encodeToString(ListSerializer(String.serializer()), it) }

    private fun decodeJsonList(raw: String?): List<String>? =
        raw?.let { Json.decodeFromString(ListSerializer(String.serializer()), it) }
}
