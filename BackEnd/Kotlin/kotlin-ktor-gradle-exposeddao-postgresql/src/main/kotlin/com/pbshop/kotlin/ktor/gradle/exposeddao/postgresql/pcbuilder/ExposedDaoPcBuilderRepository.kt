package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.pcbuilder

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PcBuildPartsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PcBuildsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PcCompatibilityRulesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PriceEntriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SellersTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ExposedDaoPcBuilderRepository(
    private val databaseFactory: DatabaseFactory,
) : PcBuilderRepository {
    override fun userExists(userId: Int): Boolean =
        databaseFactory.withTransaction { !UsersTable.selectAll().where { UsersTable.id eq userId }.empty() }

    override fun listMyBuilds(userId: Int, page: Int, limit: Int): PcBuildListResult =
        databaseFactory.withTransaction {
            val rows =
                PcBuildsTable.selectAll()
                    .where { (PcBuildsTable.user eq userId) and PcBuildsTable.deletedAt.isNull() }
                    .orderBy(PcBuildsTable.updatedAt to SortOrder.DESC)
                    .toList()
            paginateRows(rows, page, limit)
        }

    override fun listPopularBuilds(page: Int, limit: Int): PcBuildListResult =
        databaseFactory.withTransaction {
            val rows =
                PcBuildsTable.selectAll()
                    .where { PcBuildsTable.deletedAt.isNull() }
                    .orderBy(PcBuildsTable.viewCount to SortOrder.DESC, PcBuildsTable.updatedAt to SortOrder.DESC)
                    .toList()
            paginateRows(rows, page, limit)
        }

    override fun createBuild(userId: Int, newBuild: NewPcBuild): PcBuildDetailRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val buildId =
                PcBuildsTable.insert {
                    it[user] = EntityID(userId, UsersTable)
                    it[name] = newBuild.name
                    it[description] = newBuild.description
                    it[purpose] = newBuild.purpose
                    it[budget] = newBuild.budget
                    it[totalPrice] = 0
                    it[shareCode] = null
                    it[viewCount] = 0
                    it[createdAt] = now
                    it[updatedAt] = now
                    it[deletedAt] = null
                } get PcBuildsTable.id
            findBuildById(buildId.value) ?: error("Build ${buildId.value} not found")
        }

    override fun findBuildById(buildId: Int): PcBuildDetailRecord? =
        databaseFactory.withTransaction {
            PcBuildsTable.selectAll()
                .where { (PcBuildsTable.id eq buildId) and PcBuildsTable.deletedAt.isNull() }
                .singleOrNull()
                ?.let(::toDetailRecord)
        }

    override fun findOwnedBuild(userId: Int, buildId: Int): PcBuildDetailRecord? =
        databaseFactory.withTransaction {
            PcBuildsTable.selectAll()
                .where { (PcBuildsTable.id eq buildId) and (PcBuildsTable.user eq userId) and PcBuildsTable.deletedAt.isNull() }
                .singleOrNull()
                ?.let(::toDetailRecord)
        }

    override fun findBuildByShareCode(shareCode: String): PcBuildDetailRecord? =
        databaseFactory.withTransaction {
            PcBuildsTable.selectAll()
                .where { (PcBuildsTable.shareCode eq shareCode) and PcBuildsTable.deletedAt.isNull() }
                .singleOrNull()
                ?.let(::toDetailRecord)
        }

    override fun updateBuild(buildId: Int, update: PcBuildUpdate): PcBuildDetailRecord =
        databaseFactory.withTransaction {
            val current = findBuildById(buildId) ?: error("Build $buildId not found")
            PcBuildsTable.update({ PcBuildsTable.id eq buildId }) {
                it[name] = update.name ?: current.name
                it[description] = update.description ?: current.description
                it[purpose] = update.purpose ?: current.purpose
                it[budget] = update.budget ?: current.budget
                it[updatedAt] = Instant.now()
            }
            findBuildById(buildId) ?: error("Build $buildId not found")
        }

    override fun deleteBuild(buildId: Int): Boolean =
        databaseFactory.withTransaction {
            PcBuildsTable.update({ PcBuildsTable.id eq buildId }) {
                it[deletedAt] = Instant.now()
                it[updatedAt] = Instant.now()
            } > 0
        }

    override fun incrementViewCount(buildId: Int) {
        databaseFactory.withTransaction {
            val current = PcBuildsTable.selectAll().where { PcBuildsTable.id eq buildId }.singleOrNull() ?: return@withTransaction
            PcBuildsTable.update({ PcBuildsTable.id eq buildId }) {
                it[viewCount] = current[PcBuildsTable.viewCount] + 1
                it[updatedAt] = Instant.now()
            }
        }
    }

    override fun assignShareCode(buildId: Int, shareCode: String): PcBuildDetailRecord =
        databaseFactory.withTransaction {
            PcBuildsTable.update({ PcBuildsTable.id eq buildId }) {
                it[PcBuildsTable.shareCode] = shareCode
                it[updatedAt] = Instant.now()
            }
            findBuildById(buildId) ?: error("Build $buildId not found")
        }

    override fun productExists(productId: Int): Boolean =
        databaseFactory.withTransaction { !ProductsTable.selectAll().where { ProductsTable.id eq productId }.empty() }

    override fun findPriceCandidate(productId: Int, sellerId: Int?): PcBuildPartRecord? =
        databaseFactory.withTransaction {
            val rows =
                PriceEntriesTable.innerJoin(ProductsTable).innerJoin(SellersTable)
                    .selectAll()
                    .where {
                        (PriceEntriesTable.product eq productId) and
                            (sellerId?.let { PriceEntriesTable.seller eq it } ?: org.jetbrains.exposed.sql.Op.TRUE)
                    }
                    .orderBy(PriceEntriesTable.totalPrice to SortOrder.ASC)
                    .toList()
            rows.firstOrNull()?.let { row ->
                PcBuildPartRecord(
                    id = 0,
                    buildId = 0,
                    productId = productId,
                    sellerId = row[PriceEntriesTable.seller].value,
                    partType = "",
                    quantity = 1,
                    unitPrice = row[PriceEntriesTable.price],
                    totalPrice = row[PriceEntriesTable.totalPrice],
                    product = toProductRecord(row),
                    seller = toSellerRecord(row[PriceEntriesTable.seller].value, row[SellersTable.name], row[PriceEntriesTable.price]),
                )
            }
        }

    override fun upsertBuildPart(buildId: Int, newPart: NewPcBuildPart): PcBuildDetailRecord =
        databaseFactory.withTransaction {
            val candidate = findPriceCandidate(newPart.productId, newPart.sellerId) ?: error("Price candidate not found")
            val existing =
                PcBuildPartsTable.selectAll()
                    .where { (PcBuildPartsTable.build eq buildId) and (PcBuildPartsTable.partType eq newPart.partType) }
                    .singleOrNull()
            val now = Instant.now()
            if (existing == null) {
                PcBuildPartsTable.insert {
                    it[build] = EntityID(buildId, PcBuildsTable)
                    it[product] = EntityID(newPart.productId, ProductsTable)
                    it[seller] = EntityID(candidate.sellerId, SellersTable)
                    it[partType] = newPart.partType
                    it[quantity] = newPart.quantity
                    it[unitPrice] = candidate.unitPrice
                    it[totalPrice] = candidate.unitPrice * newPart.quantity
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            } else {
                PcBuildPartsTable.update({ PcBuildPartsTable.id eq existing[PcBuildPartsTable.id].value }) {
                    it[product] = EntityID(newPart.productId, ProductsTable)
                    it[seller] = EntityID(candidate.sellerId, SellersTable)
                    it[partType] = newPart.partType
                    it[quantity] = newPart.quantity
                    it[unitPrice] = candidate.unitPrice
                    it[totalPrice] = candidate.unitPrice * newPart.quantity
                    it[updatedAt] = now
                }
            }
            syncBuildTotal(buildId)
            findBuildById(buildId) ?: error("Build $buildId not found")
        }

    override fun removeBuildPart(buildId: Int, partId: Int): PcBuildDetailRecord? =
        databaseFactory.withTransaction {
            val deleted = PcBuildPartsTable.deleteWhere { (PcBuildPartsTable.id eq partId) and (PcBuildPartsTable.build eq buildId) }
            if (deleted == 0) return@withTransaction null
            syncBuildTotal(buildId)
            findBuildById(buildId)
        }

    override fun listCompatibilityRules(): List<CompatibilityRuleRecord> =
        databaseFactory.withTransaction {
            PcCompatibilityRulesTable.selectAll().orderBy(PcCompatibilityRulesTable.id to SortOrder.ASC).map(::toRuleRecord)
        }

    override fun findCompatibilityRule(ruleId: Int): CompatibilityRuleRecord? =
        databaseFactory.withTransaction {
            PcCompatibilityRulesTable.selectAll().where { PcCompatibilityRulesTable.id eq ruleId }.singleOrNull()?.let(::toRuleRecord)
        }

    override fun createCompatibilityRule(newRule: NewCompatibilityRule): CompatibilityRuleRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val ruleId =
                PcCompatibilityRulesTable.insert {
                    it[partType] = newRule.partType
                    it[targetPartType] = newRule.targetPartType
                    it[title] = newRule.title
                    it[description] = newRule.description
                    it[severity] = newRule.severity
                    it[enabled] = newRule.enabled
                    it[metadata] = newRule.metadata?.let(::encodeMetadata)
                    it[createdAt] = now
                    it[updatedAt] = now
                } get PcCompatibilityRulesTable.id
            findCompatibilityRule(ruleId.value) ?: error("Rule ${ruleId.value} not found")
        }

    override fun updateCompatibilityRule(ruleId: Int, update: CompatibilityRuleUpdate): CompatibilityRuleRecord =
        databaseFactory.withTransaction {
            val current = findCompatibilityRule(ruleId) ?: error("Rule $ruleId not found")
            PcCompatibilityRulesTable.update({ PcCompatibilityRulesTable.id eq ruleId }) {
                it[partType] = update.partType ?: current.partType
                it[targetPartType] = update.targetPartType ?: current.targetPartType
                it[title] = update.title ?: current.title
                it[description] = update.description ?: current.description
                it[severity] = update.severity ?: current.severity
                it[enabled] = update.enabled ?: current.enabled
                it[metadata] = update.metadata?.let(::encodeMetadata) ?: current.metadata?.let(::encodeMetadata)
                it[updatedAt] = Instant.now()
            }
            findCompatibilityRule(ruleId) ?: error("Rule $ruleId not found")
        }

    override fun deleteCompatibilityRule(ruleId: Int): Boolean =
        databaseFactory.withTransaction { PcCompatibilityRulesTable.deleteWhere { PcCompatibilityRulesTable.id eq ruleId } > 0 }

    private fun paginateRows(rows: List<ResultRow>, page: Int, limit: Int): PcBuildListResult {
        val offset = (page - 1).coerceAtLeast(0) * limit
        return PcBuildListResult(rows.drop(offset).take(limit).map(::toSummaryRecord), rows.size)
    }

    private fun syncBuildTotal(buildId: Int) {
        val total = PcBuildPartsTable.selectAll().where { PcBuildPartsTable.build eq buildId }.sumOf { it[PcBuildPartsTable.totalPrice] }
        PcBuildsTable.update({ PcBuildsTable.id eq buildId }) {
            it[totalPrice] = total
            it[updatedAt] = Instant.now()
        }
    }

    private fun toSummaryRecord(row: ResultRow): PcBuildSummaryRecord {
        val detail = toDetailRecord(row)
        return PcBuildSummaryRecord(
            id = detail.id,
            userId = detail.userId,
            name = detail.name,
            description = detail.description,
            purpose = detail.purpose,
            budget = detail.budget,
            totalPrice = detail.totalPrice,
            shareCode = detail.shareCode,
            viewCount = detail.viewCount,
            parts = detail.parts,
            createdAt = detail.createdAt,
            updatedAt = detail.updatedAt,
        )
    }

    private fun toDetailRecord(row: ResultRow): PcBuildDetailRecord =
        PcBuildDetailRecord(
            id = row[PcBuildsTable.id].value,
            userId = row[PcBuildsTable.user].value,
            name = row[PcBuildsTable.name],
            description = row[PcBuildsTable.description],
            purpose = row[PcBuildsTable.purpose],
            budget = row[PcBuildsTable.budget],
            totalPrice = row[PcBuildsTable.totalPrice],
            shareCode = row[PcBuildsTable.shareCode],
            viewCount = row[PcBuildsTable.viewCount],
            parts = loadParts(row[PcBuildsTable.id].value),
            createdAt = row[PcBuildsTable.createdAt],
            updatedAt = row[PcBuildsTable.updatedAt],
        )

    private fun loadParts(buildId: Int): List<PcBuildPartRecord> =
        PcBuildPartsTable.innerJoin(ProductsTable).innerJoin(SellersTable)
            .selectAll()
            .where { PcBuildPartsTable.build eq buildId }
            .orderBy(PcBuildPartsTable.id to SortOrder.ASC)
            .map { row ->
                PcBuildPartRecord(
                    id = row[PcBuildPartsTable.id].value,
                    buildId = row[PcBuildPartsTable.build].value,
                    productId = row[PcBuildPartsTable.product].value,
                    sellerId = row[PcBuildPartsTable.seller].value,
                    partType = row[PcBuildPartsTable.partType],
                    quantity = row[PcBuildPartsTable.quantity],
                    unitPrice = row[PcBuildPartsTable.unitPrice],
                    totalPrice = row[PcBuildPartsTable.totalPrice],
                    product = toProductRecord(row),
                    seller = toSellerRecord(row[PcBuildPartsTable.seller].value, row[SellersTable.name], row[PcBuildPartsTable.unitPrice]),
                )
            }

    private fun toProductRecord(row: ResultRow): PcBuildProductRecord =
        PcBuildProductRecord(
            id = row[ProductsTable.id].value,
            name = row[ProductsTable.name],
            thumbnailUrl = row[ProductsTable.thumbnailUrl],
            lowestPrice = row[ProductsTable.lowestPrice] ?: row[ProductsTable.price],
        )

    private fun toSellerRecord(id: Int, name: String, price: Int): PcBuildSellerRecord = PcBuildSellerRecord(id = id, name = name, price = price)

    private fun toRuleRecord(row: ResultRow): CompatibilityRuleRecord =
        CompatibilityRuleRecord(
            id = row[PcCompatibilityRulesTable.id].value,
            partType = row[PcCompatibilityRulesTable.partType],
            targetPartType = row[PcCompatibilityRulesTable.targetPartType],
            title = row[PcCompatibilityRulesTable.title],
            description = row[PcCompatibilityRulesTable.description],
            severity = row[PcCompatibilityRulesTable.severity],
            enabled = row[PcCompatibilityRulesTable.enabled],
            metadata = row[PcCompatibilityRulesTable.metadata]?.let(::decodeMetadata),
            createdAt = row[PcCompatibilityRulesTable.createdAt],
            updatedAt = row[PcCompatibilityRulesTable.updatedAt],
        )

    private fun encodeMetadata(metadata: Map<String, String>): String =
        metadata.entries.joinToString(prefix = "{", postfix = "}") { (key, value) -> "\"$key\":\"$value\"" }

    private fun decodeMetadata(raw: String): Map<String, String> =
        raw.removePrefix("{").removeSuffix("}")
            .split(",")
            .mapNotNull {
                val parts = it.split(":")
                if (parts.size == 2) parts[0].trim().trim('"') to parts[1].trim().trim('"') else null
            }.toMap()
}
