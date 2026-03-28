package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductStatus
import java.time.Instant

class InMemoryProductRepository(
    seededProducts: List<ProductRecord> = emptyList(),
    seededOptions: List<ProductOptionRecord> = emptyList(),
    seededImages: List<ProductImageRecord> = emptyList(),
    seededSpecs: Map<Int, List<ProductSpecRecord>> = emptyMap(),
    seededPrices: Map<Int, List<ProductPriceEntryRecord>> = emptyMap(),
    seededCategories: Map<Int, String> = emptyMap(),
) : ProductRepository {
    private val products = linkedMapOf<Int, ProductRecord>()
    private val options = linkedMapOf<Int, ProductOptionRecord>()
    private val images = linkedMapOf<Int, ProductImageRecord>()
    private val specsByProduct = seededSpecs.mapValues { it.value.toMutableList() }.toMutableMap()
    private val pricesByProduct = seededPrices.mapValues { it.value.toMutableList() }.toMutableMap()
    private val categoryNames = seededCategories.toMutableMap()
    private var nextProductId = 1
    private var nextOptionId = 1
    private var nextImageId = 1

    init {
        seededProducts.forEach {
            products[it.id] = it
            nextProductId = maxOf(nextProductId, it.id + 1)
        }
        seededOptions.forEach {
            options[it.id] = it
            nextOptionId = maxOf(nextOptionId, it.id + 1)
        }
        seededImages.forEach {
            images[it.id] = it
            nextImageId = maxOf(nextImageId, it.id + 1)
        }
    }

    override fun listProducts(query: ProductListQuery): ProductListResult {
        val filtered =
            products.values
                .asSequence()
                .filter { it.deletedAt == null }
                .filter { query.categoryId == null || it.categoryId == query.categoryId }
                .filter { query.search.isNullOrBlank() || it.name.contains(query.search, ignoreCase = true) }
                .filter { query.minPrice == null || effectivePrice(it) >= query.minPrice }
                .filter { query.maxPrice == null || effectivePrice(it) <= query.maxPrice }
                .filter { query.specFilters.isEmpty() || matchesSpecs(it.id, query.specFilters) }
                .sortedWith(sortComparator(query.sort))
                .toList()

        val offset = (query.page - 1) * query.limit
        return ProductListResult(
            items = filtered.drop(offset).take(query.limit),
            totalCount = filtered.size,
        )
    }

    override fun findProductById(id: Int): ProductRecord? = products[id]?.takeIf { it.deletedAt == null }

    override fun findProductDetailById(id: Int): ProductDetailRecord? {
        val product = findProductById(id) ?: return null
        val priceEntries = pricesByProduct[id].orEmpty().sortedBy { it.price }
        val prices = priceEntries.map { it.price }
        return ProductDetailRecord(
            product = product,
            highestPrice = prices.maxOrNull() ?: product.price,
            averagePrice = if (prices.isEmpty()) product.price else prices.average().toInt(),
            options = options.values.filter { it.productId == id }.sortedBy { it.id },
            images = images.values.filter { it.productId == id }.sortedBy { it.sortOrder },
            specs = specsByProduct[id].orEmpty(),
            priceEntries = priceEntries,
        )
    }

    override fun categoryExists(categoryId: Int): Boolean = categoryNames.containsKey(categoryId)

    override fun createProduct(
        newProduct: NewProduct,
        options: List<NewProductOption>,
        images: List<NewProductImage>,
    ): ProductDetailRecord {
        val now = Instant.now()
        val created =
            ProductRecord(
                id = nextProductId++,
                name = newProduct.name,
                description = newProduct.description,
                price = newProduct.price,
                discountPrice = newProduct.discountPrice,
                stock = newProduct.stock,
                status = newProduct.status,
                categoryId = newProduct.categoryId,
                categoryName = categoryNames[newProduct.categoryId],
                thumbnailUrl = newProduct.thumbnailUrl,
                lowestPrice = newProduct.discountPrice ?: newProduct.price,
                sellerCount = 0,
                reviewCount = 0,
                averageRating = 0.0,
                popularityScore = 0.0,
                createdAt = now,
                deletedAt = null,
            )
        products[created.id] = created
        replaceOptions(created.id, options)
        replaceImages(created.id, images)
        return requireNotNull(findProductDetailById(created.id))
    }

    override fun updateProduct(
        productId: Int,
        update: ProductUpdate,
        replaceOptions: List<NewProductOption>?,
        replaceImages: List<NewProductImage>?,
    ): ProductDetailRecord {
        val current = products[productId] ?: error("Product $productId not found")
        val updated =
            current.copy(
                name = update.name ?: current.name,
                description = update.description ?: current.description,
                price = update.price ?: current.price,
                discountPrice = update.discountPrice ?: current.discountPrice,
                stock = update.stock ?: current.stock,
                status = update.status ?: current.status,
                categoryId = update.categoryId ?: current.categoryId,
                categoryName = categoryNames[update.categoryId ?: current.categoryId],
                thumbnailUrl = update.thumbnailUrl ?: current.thumbnailUrl,
                lowestPrice =
                    if (current.sellerCount == 0) {
                        update.discountPrice ?: update.price ?: current.discountPrice ?: current.price
                    } else {
                        current.lowestPrice
                    },
            )
        products[productId] = updated
        replaceOptions?.let { this.replaceOptions(productId, it) }
        replaceImages?.let { this.replaceImages(productId, it) }
        return requireNotNull(findProductDetailById(productId))
    }

    override fun softDeleteProduct(productId: Int) {
        val current = products[productId] ?: error("Product $productId not found")
        products[productId] = current.copy(deletedAt = Instant.now())
    }

    override fun createOption(
        productId: Int,
        newOption: NewProductOption,
    ): ProductOptionRecord {
        val created = ProductOptionRecord(nextOptionId++, productId, newOption.name, newOption.values)
        options[created.id] = created
        return created
    }

    override fun updateOption(
        productId: Int,
        optionId: Int,
        update: ProductOptionUpdate,
    ): ProductOptionRecord {
        val current = options[optionId]?.takeIf { it.productId == productId } ?: error("Option $optionId not found")
        val updated = current.copy(name = update.name, values = update.values)
        options[optionId] = updated
        return updated
    }

    override fun deleteOption(
        productId: Int,
        optionId: Int,
    ) {
        val current = options[optionId]?.takeIf { it.productId == productId } ?: error("Option $optionId not found")
        options.remove(current.id)
    }

    override fun createImage(
        productId: Int,
        newImage: NewProductImage,
    ): ProductImageRecord {
        if (newImage.isMain) {
            images.replaceAll { _, image ->
                if (image.productId == productId) image.copy(isMain = false) else image
            }
        }
        val created = ProductImageRecord(nextImageId++, productId, newImage.url, newImage.isMain, newImage.sortOrder, newImage.imageVariantId)
        images[created.id] = created
        return created
    }

    override fun deleteImage(
        productId: Int,
        imageId: Int,
    ) {
        val current = images[imageId]?.takeIf { it.productId == productId } ?: error("Image $imageId not found")
        images.remove(current.id)
    }

    private fun replaceOptions(
        productId: Int,
        replacements: List<NewProductOption>,
    ) {
        options.values.removeIf { it.productId == productId }
        replacements.forEach { createOption(productId, it) }
    }

    private fun replaceImages(
        productId: Int,
        replacements: List<NewProductImage>,
    ) {
        images.values.removeIf { it.productId == productId }
        replacements.forEach { createImage(productId, it) }
    }

    private fun effectivePrice(record: ProductRecord): Int = record.lowestPrice ?: record.price

    private fun matchesSpecs(
        productId: Int,
        filters: Map<String, String>,
    ): Boolean {
        val specs = specsByProduct[productId].orEmpty()
        return filters.all { (name, value) ->
            specs.any { it.name.equals(name, ignoreCase = true) && it.value.equals(value, ignoreCase = true) }
        }
    }

    private fun sortComparator(sort: ProductSort): Comparator<ProductRecord> =
        when (sort) {
            ProductSort.PRICE_ASC -> compareBy<ProductRecord> { effectivePrice(it) }.thenBy { it.id }
            ProductSort.PRICE_DESC -> compareByDescending<ProductRecord> { effectivePrice(it) }.thenBy { it.id }
            ProductSort.POPULARITY -> compareByDescending<ProductRecord> { it.popularityScore }.thenBy { it.id }
            ProductSort.RATING_DESC -> compareByDescending<ProductRecord> { it.averageRating }.thenByDescending { it.reviewCount }.thenBy { it.id }
            ProductSort.RATING_ASC -> compareBy<ProductRecord> { it.averageRating }.thenBy { it.id }
            ProductSort.NEWEST -> compareByDescending<ProductRecord> { it.createdAt }.thenBy { it.id }
        }

    companion object {
        fun seeded(): InMemoryProductRepository {
            val now = Instant.now()
            return InMemoryProductRepository(
                seededProducts =
                    listOf(
                        ProductRecord(1, "게이밍 노트북 A15", "RTX 탑재 게이밍 노트북", 1890000, 1790000, 30, ProductStatus.ON_SALE, 2, "노트북", "https://img.example.com/p1-thumb.jpg", 1720000, 2, 2, 4.5, 95.5, now.minusSeconds(86400), null),
                        ProductRecord(2, "사무용 노트북 Slim", "가벼운 사무용 노트북", 990000, null, 50, ProductStatus.ON_SALE, 2, "노트북", "https://img.example.com/p2-thumb.jpg", 940000, 2, 1, 4.0, 71.2, now.minusSeconds(80000), null),
                        ProductRecord(3, "미니 데스크탑 Pro", "개발용 데스크탑", 1450000, 1390000, 16, ProductStatus.ON_SALE, 3, "데스크탑", "https://img.example.com/p3-thumb.jpg", 1360000, 2, 1, 5.0, 66.7, now.minusSeconds(76000), null),
                    ),
                seededOptions =
                    listOf(
                        ProductOptionRecord(1, 1, "RAM", listOf("16GB", "32GB")),
                        ProductOptionRecord(2, 1, "SSD", listOf("512GB", "1TB")),
                        ProductOptionRecord(3, 2, "색상", listOf("실버", "스페이스그레이")),
                    ),
                seededImages =
                    listOf(
                        ProductImageRecord(1, 1, "https://img.example.com/p1-1.jpg", true, 1),
                        ProductImageRecord(2, 1, "https://img.example.com/p1-2.jpg", false, 2),
                        ProductImageRecord(3, 2, "https://img.example.com/p2-1.jpg", true, 1),
                    ),
                seededSpecs =
                    mapOf(
                        1 to listOf(ProductSpecRecord("CPU", "Intel i7-14700H"), ProductSpecRecord("RAM", "16"), ProductSpecRecord("SSD", "1024")),
                        2 to listOf(ProductSpecRecord("CPU", "Intel i5-13420H"), ProductSpecRecord("RAM", "16")),
                        3 to listOf(ProductSpecRecord("GPU", "RTX 4060")),
                    ),
                seededPrices =
                    mapOf(
                        1 to listOf(ProductPriceEntryRecord(1, "공식몰", "https://img.example.com/s1-logo.png", 92, 1720000, "https://official.example.com/p/1", "무료배송"), ProductPriceEntryRecord(2, "테크마켓", "https://img.example.com/s2-logo.png", 84, 1750000, "https://techmarket.example.com/p/1", "기본배송")),
                        2 to listOf(ProductPriceEntryRecord(1, "공식몰", "https://img.example.com/s1-logo.png", 92, 940000, "https://official.example.com/p/2", "무료배송"), ProductPriceEntryRecord(2, "테크마켓", "https://img.example.com/s2-logo.png", 84, 955000, "https://techmarket.example.com/p/2", "조건부무료")),
                        3 to listOf(ProductPriceEntryRecord(1, "공식몰", "https://img.example.com/s1-logo.png", 92, 1360000, "https://official.example.com/p/3", "무료배송")),
                    ),
                seededCategories = mapOf(1 to "컴퓨터", 2 to "노트북", 3 to "데스크탑", 4 to "자동차", 5 to "전기차"),
            )
        }
    }
}
