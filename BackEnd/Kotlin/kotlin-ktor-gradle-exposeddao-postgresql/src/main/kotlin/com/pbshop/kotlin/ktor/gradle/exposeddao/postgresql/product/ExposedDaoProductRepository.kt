package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.CategoriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PriceEntriesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductImageEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductImagesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductOptionEntity
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductOptionsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductSpecsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SellersTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.SpecDefinitionsTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.time.Instant

class ExposedDaoProductRepository(
    private val databaseFactory: DatabaseFactory,
) : ProductRepository {
    override fun listProducts(query: ProductListQuery): ProductListResult =
        databaseFactory.withTransaction {
            val matchingIds = matchingProductIdsForSpecs(query.specFilters)
            if (matchingIds != null && matchingIds.isEmpty()) {
                return@withTransaction ProductListResult(emptyList(), 0)
            }

            var predicate: Op<Boolean> = ProductsTable.deletedAt.isNull()
            query.categoryId?.let { predicate = predicate and (ProductsTable.category eq it) }
            query.search?.let { predicate = predicate and (ProductsTable.name like "%$it%") }
            query.minPrice?.let {
                predicate =
                    predicate and (
                        (ProductsTable.lowestPrice greaterEq it) or
                            (ProductsTable.lowestPrice.isNull() and (ProductsTable.price greaterEq it))
                    )
            }
            query.maxPrice?.let {
                predicate =
                    predicate and (
                        (ProductsTable.lowestPrice lessEq it) or
                            (ProductsTable.lowestPrice.isNull() and (ProductsTable.price lessEq it))
                    )
            }
            matchingIds?.let { predicate = predicate and (ProductsTable.id inList it) }

            val rows =
                ProductsTable
                    .innerJoin(CategoriesTable)
                    .selectAll()
                    .where { predicate }
                    .map(::toProductRecord)
                    .sortedWith(sortComparator(query.sort))

            val offset = (query.page - 1) * query.limit
            ProductListResult(rows.drop(offset).take(query.limit), rows.size)
        }

    override fun findProductById(id: Int): ProductRecord? =
        databaseFactory.withTransaction {
            ProductsTable
                .innerJoin(CategoriesTable)
                .selectAll()
                .where { (ProductsTable.id eq id) and ProductsTable.deletedAt.isNull() }
                .limit(1)
                .firstOrNull()
                ?.let(::toProductRecord)
        }

    override fun findProductDetailById(id: Int): ProductDetailRecord? =
        databaseFactory.withTransaction {
            val product =
                ProductsTable
                    .innerJoin(CategoriesTable)
                    .selectAll()
                    .where { (ProductsTable.id eq id) and ProductsTable.deletedAt.isNull() }
                    .limit(1)
                    .firstOrNull()
                    ?.let(::toProductRecord)
                    ?: return@withTransaction null

            val optionRows =
                ProductOptionsTable
                    .selectAll()
                    .where { ProductOptionsTable.product eq id }
                    .orderBy(ProductOptionsTable.id to SortOrder.ASC)
                    .map {
                        ProductOptionRecord(
                            id = it[ProductOptionsTable.id].value,
                            productId = it[ProductOptionsTable.product].value,
                            name = it[ProductOptionsTable.name],
                            values = decodeOptionValues(it[ProductOptionsTable.optionValues]),
                        )
                    }

            val imageRows =
                ProductImagesTable
                    .selectAll()
                    .where { ProductImagesTable.product eq id }
                    .orderBy(ProductImagesTable.sortOrder to SortOrder.ASC, ProductImagesTable.id to SortOrder.ASC)
                    .map {
                        ProductImageRecord(
                            id = it[ProductImagesTable.id].value,
                            productId = it[ProductImagesTable.product].value,
                            url = it[ProductImagesTable.url],
                            isMain = it[ProductImagesTable.isMain],
                            sortOrder = it[ProductImagesTable.sortOrder],
                            imageVariantId = it[ProductImagesTable.imageVariantId],
                        )
                    }

            val specRows =
                ProductSpecsTable
                    .innerJoin(SpecDefinitionsTable)
                    .selectAll()
                    .where { ProductSpecsTable.product eq id }
                    .orderBy(SpecDefinitionsTable.sortOrder to SortOrder.ASC, SpecDefinitionsTable.id to SortOrder.ASC)
                    .map {
                        ProductSpecRecord(
                            name = it[SpecDefinitionsTable.name],
                            value = it[ProductSpecsTable.value],
                        )
                    }

            val priceRows =
                PriceEntriesTable
                    .innerJoin(SellersTable)
                    .selectAll()
                    .where { (PriceEntriesTable.product eq id) and (PriceEntriesTable.isAvailable eq true) }
                    .orderBy(PriceEntriesTable.price to SortOrder.ASC, SellersTable.id to SortOrder.ASC)
                    .map {
                        ProductPriceEntryRecord(
                            sellerId = it[SellersTable.id].value,
                            sellerName = it[SellersTable.name],
                            sellerLogoUrl = it[SellersTable.logoUrl],
                            trustScore = it[SellersTable.trustScore],
                            price = it[PriceEntriesTable.price],
                            url = it[PriceEntriesTable.productUrl],
                            shipping = it[PriceEntriesTable.shippingInfo] ?: it[PriceEntriesTable.shippingType].name,
                        )
                    }

            ProductDetailRecord(
                product = product,
                highestPrice = priceRows.maxOfOrNull { it.price } ?: product.price,
                averagePrice = if (priceRows.isEmpty()) product.price else priceRows.map { it.price }.average().toInt(),
                options = optionRows,
                images = imageRows,
                specs = specRows,
                priceEntries = priceRows,
            )
        }

    override fun categoryExists(categoryId: Int): Boolean =
        databaseFactory.withTransaction {
            !CategoriesTable.selectAll().where { CategoriesTable.id eq categoryId }.limit(1).empty()
        }

    override fun createProduct(
        newProduct: NewProduct,
        options: List<NewProductOption>,
        images: List<NewProductImage>,
    ): ProductDetailRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val created =
                ProductEntity.new {
                    name = newProduct.name
                    description = newProduct.description
                    price = newProduct.price
                    discountPrice = newProduct.discountPrice
                    stock = newProduct.stock
                    status = newProduct.status
                    categoryId = EntityID(newProduct.categoryId, CategoriesTable)
                    thumbnailUrl = newProduct.thumbnailUrl
                    lowestPrice = newProduct.discountPrice ?: newProduct.price
                    sellerCount = 0
                    viewCount = 0
                    reviewCount = 0
                    averageRating = BigDecimal.ZERO.setScale(1)
                    salesCount = 0
                    popularityScore = BigDecimal.ZERO.setScale(2)
                    version = 1
                    createdAt = now
                    updatedAt = now
                    deletedAt = null
                }
            createOptions(created.id.value, options, now)
            createImages(created.id.value, images, now)
            requireNotNull(findProductDetailById(created.id.value))
        }

    override fun updateProduct(
        productId: Int,
        update: ProductUpdate,
        replaceOptions: List<NewProductOption>?,
        replaceImages: List<NewProductImage>?,
    ): ProductDetailRecord =
        databaseFactory.withTransaction {
            val entity = requireNotNull(ProductEntity.findById(productId)) { "Product $productId not found" }
            entity.apply {
                name = update.name ?: name
                description = update.description ?: description
                price = update.price ?: price
                discountPrice = update.discountPrice ?: discountPrice
                stock = update.stock ?: stock
                status = update.status ?: status
                categoryId = update.categoryId?.let { EntityID(it, CategoriesTable) } ?: categoryId
                thumbnailUrl = update.thumbnailUrl ?: thumbnailUrl
                if (sellerCount == 0) {
                    lowestPrice = discountPrice ?: price
                }
                updatedAt = Instant.now()
            }
            replaceOptions?.let {
                ProductOptionEntity.find { ProductOptionsTable.product eq productId }.forEach { option -> option.delete() }
                createOptions(productId, it, Instant.now())
            }
            replaceImages?.let {
                ProductImageEntity.find { ProductImagesTable.product eq productId }.forEach { image -> image.delete() }
                createImages(productId, it, Instant.now())
            }
            requireNotNull(findProductDetailById(productId))
        }

    override fun softDeleteProduct(productId: Int) {
        databaseFactory.withTransaction {
            ProductEntity.findById(productId)?.apply {
                deletedAt = Instant.now()
                updatedAt = Instant.now()
            }
        }
    }

    override fun createOption(
        productId: Int,
        newOption: NewProductOption,
    ): ProductOptionRecord =
        databaseFactory.withTransaction {
            val created =
                ProductOptionEntity.new {
                    this.productId = EntityID(productId, ProductsTable)
                    name = newOption.name
                    optionValuesJson = encodeOptionValues(newOption.values)
                    createdAt = Instant.now()
                    updatedAt = Instant.now()
                }
            ProductOptionRecord(created.id.value, productId, created.name, decodeOptionValues(created.optionValuesJson))
        }

    override fun updateOption(
        productId: Int,
        optionId: Int,
        update: ProductOptionUpdate,
    ): ProductOptionRecord =
        databaseFactory.withTransaction {
            val entity = ProductOptionEntity.findById(optionId)?.takeIf { it.productId.value == productId } ?: error("Option $optionId not found")
            entity.apply {
                name = update.name
                optionValuesJson = encodeOptionValues(update.values)
                updatedAt = Instant.now()
            }
            ProductOptionRecord(entity.id.value, productId, entity.name, decodeOptionValues(entity.optionValuesJson))
        }

    override fun deleteOption(
        productId: Int,
        optionId: Int,
    ) {
        databaseFactory.withTransaction {
            ProductOptionEntity.findById(optionId)?.takeIf { it.productId.value == productId }?.delete() ?: error("Option $optionId not found")
        }
    }

    override fun createImage(
        productId: Int,
        newImage: NewProductImage,
    ): ProductImageRecord =
        databaseFactory.withTransaction {
            if (newImage.isMain) {
                ProductImageEntity.find { ProductImagesTable.product eq productId }.forEach { image -> image.isMain = false }
            }
            val created =
                ProductImageEntity.new {
                    this.productId = EntityID(productId, ProductsTable)
                    url = newImage.url
                    isMain = newImage.isMain
                    sortOrder = newImage.sortOrder
                    imageVariantId = newImage.imageVariantId
                    createdAt = Instant.now()
                }
            ProductImageRecord(created.id.value, productId, created.url, created.isMain, created.sortOrder, created.imageVariantId)
        }

    override fun deleteImage(
        productId: Int,
        imageId: Int,
    ) {
        databaseFactory.withTransaction {
            ProductImageEntity.findById(imageId)?.takeIf { it.productId.value == productId }?.delete() ?: error("Image $imageId not found")
        }
    }

    private fun matchingProductIdsForSpecs(specFilters: Map<String, String>): Set<Int>? {
        if (specFilters.isEmpty()) return null
        return specFilters.entries
            .map { (name, value) ->
                ProductSpecsTable.innerJoin(SpecDefinitionsTable)
                    .selectAll()
                    .where { (SpecDefinitionsTable.name eq name) and (ProductSpecsTable.value eq value) }
                    .map { it[ProductSpecsTable.product].value }
                    .toSet()
            }.reduceOrNull(Set<Int>::intersect) ?: emptySet()
    }

    private fun createOptions(
        productId: Int,
        options: List<NewProductOption>,
        now: Instant,
    ) {
        options.forEach { option ->
            ProductOptionEntity.new {
                this.productId = EntityID(productId, ProductsTable)
                name = option.name
                optionValuesJson = encodeOptionValues(option.values)
                createdAt = now
                updatedAt = now
            }
        }
    }

    private fun createImages(
        productId: Int,
        images: List<NewProductImage>,
        now: Instant,
    ) {
        var mainApplied = false
        images.forEach { image ->
            ProductImageEntity.new {
                this.productId = EntityID(productId, ProductsTable)
                url = image.url
                isMain = image.isMain && !mainApplied
                if (isMain) {
                    mainApplied = true
                }
                sortOrder = image.sortOrder
                imageVariantId = image.imageVariantId
                createdAt = now
            }
        }
    }

    private fun toProductRecord(row: ResultRow): ProductRecord =
        ProductRecord(
            id = row[ProductsTable.id].value,
            name = row[ProductsTable.name],
            description = row[ProductsTable.description],
            price = row[ProductsTable.price],
            discountPrice = row[ProductsTable.discountPrice],
            stock = row[ProductsTable.stock],
            status = row[ProductsTable.status],
            categoryId = row[ProductsTable.category].value,
            categoryName = row[CategoriesTable.name],
            thumbnailUrl = row[ProductsTable.thumbnailUrl],
            lowestPrice = row[ProductsTable.lowestPrice],
            sellerCount = row[ProductsTable.sellerCount],
            reviewCount = row[ProductsTable.reviewCount],
            averageRating = row[ProductsTable.averageRating].toDouble(),
            popularityScore = row[ProductsTable.popularityScore].toDouble(),
            createdAt = row[ProductsTable.createdAt],
            deletedAt = row[ProductsTable.deletedAt],
        )

    private fun effectivePrice(record: ProductRecord): Int = record.lowestPrice ?: record.price

    private fun encodeOptionValues(values: List<String>): String =
        Json.encodeToString(ListSerializer(String.serializer()), values)

    private fun decodeOptionValues(raw: String): List<String> =
        Json.decodeFromString(ListSerializer(String.serializer()), raw)

    private fun sortComparator(sort: ProductSort): Comparator<ProductRecord> =
        when (sort) {
            ProductSort.PRICE_ASC -> compareBy<ProductRecord> { effectivePrice(it) }.thenBy { it.id }
            ProductSort.PRICE_DESC -> compareByDescending<ProductRecord> { effectivePrice(it) }.thenBy { it.id }
            ProductSort.POPULARITY -> compareByDescending<ProductRecord> { it.popularityScore }.thenBy { it.id }
            ProductSort.RATING_DESC -> compareByDescending<ProductRecord> { it.averageRating }.thenByDescending { it.reviewCount }.thenBy { it.id }
            ProductSort.RATING_ASC -> compareBy<ProductRecord> { it.averageRating }.thenBy { it.id }
            ProductSort.NEWEST -> compareByDescending<ProductRecord> { it.createdAt }.thenBy { it.id }
        }
}
