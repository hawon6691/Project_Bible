package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.query

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PriceEntriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductQueryViewsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ExposedDaoQueryRepository(
    private val databaseFactory: DatabaseFactory,
) : QueryRepository {
    override fun listProducts(query: ProductQueryViewQuery): ProductQueryViewListResult =
        databaseFactory.withTransaction {
            val filtered =
                ProductQueryViewsTable
                    .selectAll()
                    .map(::toRecord)
                    .asSequence()
                    .filter { query.categoryId == null || it.categoryId == query.categoryId }
                    .filter { query.keyword.isNullOrBlank() || it.name.contains(query.keyword.trim(), ignoreCase = true) }
                    .filter { query.minPrice == null || (it.lowestPrice ?: it.basePrice) >= query.minPrice }
                    .filter { query.maxPrice == null || (it.lowestPrice ?: it.basePrice) <= query.maxPrice }
                    .sortedWith(sortComparator(query.sort))
                    .toList()
            val safePage = query.page.coerceAtLeast(1)
            val safeLimit = query.limit.coerceIn(1, 100)
            val offset = (safePage - 1) * safeLimit
            ProductQueryViewListResult(
                items = filtered.drop(offset).take(safeLimit),
                totalCount = filtered.size,
            )
        }

    override fun findProductDetail(productId: Int): ProductQueryViewRecord? =
        databaseFactory.withTransaction {
            ProductQueryViewsTable.selectAll().where { ProductQueryViewsTable.product eq productId }.singleOrNull()?.let(::toRecord)
        }

    override fun syncProduct(productId: Int): ProductQueryViewRecord? =
        databaseFactory.withTransaction {
            val product =
                ProductsTable.selectAll()
                    .where { (ProductsTable.id eq productId) and ProductsTable.deletedAt.isNull() }
                    .singleOrNull()
                    ?: return@withTransaction null
            val activePrices =
                PriceEntriesTable.selectAll()
                    .where { (PriceEntriesTable.product eq productId) and (PriceEntriesTable.isAvailable eq true) }
                    .toList()
            val lowestPrice = activePrices.minOfOrNull { it[PriceEntriesTable.totalPrice] }
            val sellerCount = activePrices.map { it[PriceEntriesTable.seller].value }.distinct().size
            val now = Instant.now()
            val existing = ProductQueryViewsTable.selectAll().where { ProductQueryViewsTable.product eq productId }.singleOrNull()
            if (existing == null) {
                ProductQueryViewsTable.insert {
                    fillProjectionRow(it, product, lowestPrice, sellerCount, now)
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            } else {
                ProductQueryViewsTable.update({ ProductQueryViewsTable.product eq productId }) {
                    fillProjectionRow(it, product, lowestPrice, sellerCount, now)
                    it[updatedAt] = now
                }
            }
            ProductQueryViewsTable.selectAll().where { ProductQueryViewsTable.product eq productId }.singleOrNull()?.let(::toRecord)
        }

    override fun rebuildAll(): Int =
        databaseFactory.withTransaction {
            val productIds =
                ProductsTable.selectAll()
                    .where { ProductsTable.deletedAt.isNull() }
                    .map { it[ProductsTable.id].value }
            productIds.forEach { productId ->
                syncProduct(productId)
            }
            productIds.size
        }

    private fun fillProjectionRow(
        stmt: UpdateBuilder<*>,
        product: org.jetbrains.exposed.sql.ResultRow,
        lowestPrice: Int?,
        sellerCount: Int,
        now: Instant,
    ) {
        stmt[ProductQueryViewsTable.product] = product[ProductsTable.id]
        stmt[ProductQueryViewsTable.categoryId] = product[ProductsTable.category].value
        stmt[ProductQueryViewsTable.name] = product[ProductsTable.name]
        stmt[ProductQueryViewsTable.thumbnailUrl] = product[ProductsTable.thumbnailUrl]
        stmt[ProductQueryViewsTable.status] = product[ProductsTable.status].name
        stmt[ProductQueryViewsTable.basePrice] = product[ProductsTable.discountPrice] ?: product[ProductsTable.price]
        stmt[ProductQueryViewsTable.lowestPrice] = lowestPrice
        stmt[ProductQueryViewsTable.sellerCount] = sellerCount
        stmt[ProductQueryViewsTable.averageRating] = product[ProductsTable.averageRating]
        stmt[ProductQueryViewsTable.reviewCount] = product[ProductsTable.reviewCount]
        stmt[ProductQueryViewsTable.viewCount] = product[ProductsTable.viewCount]
        stmt[ProductQueryViewsTable.popularityScore] = product[ProductsTable.popularityScore]
        stmt[ProductQueryViewsTable.syncedAt] = now
    }

    private fun toRecord(row: org.jetbrains.exposed.sql.ResultRow): ProductQueryViewRecord =
        ProductQueryViewRecord(
            productId = row[ProductQueryViewsTable.product].value,
            categoryId = row[ProductQueryViewsTable.categoryId],
            name = row[ProductQueryViewsTable.name],
            thumbnailUrl = row[ProductQueryViewsTable.thumbnailUrl],
            status = row[ProductQueryViewsTable.status],
            basePrice = row[ProductQueryViewsTable.basePrice],
            lowestPrice = row[ProductQueryViewsTable.lowestPrice],
            sellerCount = row[ProductQueryViewsTable.sellerCount],
            averageRating = row[ProductQueryViewsTable.averageRating].toDouble(),
            reviewCount = row[ProductQueryViewsTable.reviewCount],
            viewCount = row[ProductQueryViewsTable.viewCount],
            popularityScore = row[ProductQueryViewsTable.popularityScore].toDouble(),
            syncedAt = row[ProductQueryViewsTable.syncedAt],
            updatedAt = row[ProductQueryViewsTable.updatedAt],
        )

    private fun sortComparator(sort: ProductQuerySort): Comparator<ProductQueryViewRecord> =
        when (sort) {
            ProductQuerySort.PRICE_ASC ->
                compareBy<ProductQueryViewRecord> { it.lowestPrice ?: it.basePrice }.thenByDescending { it.updatedAt }
            ProductQuerySort.PRICE_DESC ->
                compareByDescending<ProductQueryViewRecord> { it.lowestPrice ?: it.basePrice }.thenByDescending { it.updatedAt }
            ProductQuerySort.POPULARITY ->
                compareByDescending<ProductQueryViewRecord> { it.popularityScore }.thenByDescending { it.viewCount }
            ProductQuerySort.RATING ->
                compareByDescending<ProductQueryViewRecord> { it.averageRating }.thenByDescending { it.reviewCount }
            ProductQuerySort.NEWEST ->
                compareByDescending<ProductQueryViewRecord> { it.updatedAt }
        }
}
