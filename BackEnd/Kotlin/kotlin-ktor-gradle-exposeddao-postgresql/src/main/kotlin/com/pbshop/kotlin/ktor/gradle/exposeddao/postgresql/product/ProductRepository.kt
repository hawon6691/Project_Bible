package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductStatus
import java.time.Instant

data class ProductRecord(
    val id: Int,
    val name: String,
    val description: String,
    val price: Int,
    val discountPrice: Int?,
    val stock: Int,
    val status: ProductStatus,
    val categoryId: Int,
    val categoryName: String?,
    val thumbnailUrl: String?,
    val lowestPrice: Int?,
    val sellerCount: Int,
    val reviewCount: Int,
    val averageRating: Double,
    val popularityScore: Double,
    val createdAt: Instant,
    val deletedAt: Instant?,
)

data class ProductOptionRecord(
    val id: Int,
    val productId: Int,
    val name: String,
    val values: List<String>,
)

data class ProductImageRecord(
    val id: Int,
    val productId: Int,
    val url: String,
    val isMain: Boolean,
    val sortOrder: Int,
    val imageVariantId: Int? = null,
)

data class ProductSpecRecord(
    val name: String,
    val value: String,
)

data class ProductPriceEntryRecord(
    val sellerId: Int,
    val sellerName: String,
    val sellerLogoUrl: String?,
    val trustScore: Int,
    val price: Int,
    val url: String,
    val shipping: String,
)

data class ProductDetailRecord(
    val product: ProductRecord,
    val highestPrice: Int,
    val averagePrice: Int,
    val options: List<ProductOptionRecord>,
    val images: List<ProductImageRecord>,
    val specs: List<ProductSpecRecord>,
    val priceEntries: List<ProductPriceEntryRecord>,
)

enum class ProductSort(
    val apiValue: String,
) {
    PRICE_ASC("price_asc"),
    PRICE_DESC("price_desc"),
    NEWEST("newest"),
    POPULARITY("popularity"),
    RATING_DESC("rating_desc"),
    RATING_ASC("rating_asc"),
    ;

    companion object {
        fun fromQuery(value: String?): ProductSort =
            entries.firstOrNull { it.apiValue == value?.trim()?.lowercase() } ?: NEWEST
    }
}

data class ProductListQuery(
    val page: Int,
    val limit: Int,
    val categoryId: Int? = null,
    val search: String? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val sort: ProductSort = ProductSort.NEWEST,
    val specFilters: Map<String, String> = emptyMap(),
)

data class ProductListResult(
    val items: List<ProductRecord>,
    val totalCount: Int,
)

data class NewProduct(
    val name: String,
    val description: String,
    val price: Int,
    val discountPrice: Int?,
    val stock: Int,
    val status: ProductStatus,
    val categoryId: Int,
    val thumbnailUrl: String?,
)

data class ProductUpdate(
    val name: String? = null,
    val description: String? = null,
    val price: Int? = null,
    val discountPrice: Int? = null,
    val stock: Int? = null,
    val status: ProductStatus? = null,
    val categoryId: Int? = null,
    val thumbnailUrl: String? = null,
)

data class NewProductOption(
    val name: String,
    val values: List<String>,
)

data class ProductOptionUpdate(
    val name: String,
    val values: List<String>,
)

data class NewProductImage(
    val url: String,
    val isMain: Boolean,
    val sortOrder: Int,
    val imageVariantId: Int? = null,
)

interface ProductRepository {
    fun listProducts(query: ProductListQuery): ProductListResult

    fun findProductById(id: Int): ProductRecord?

    fun findProductDetailById(id: Int): ProductDetailRecord?

    fun categoryExists(categoryId: Int): Boolean

    fun createProduct(
        newProduct: NewProduct,
        options: List<NewProductOption>,
        images: List<NewProductImage>,
    ): ProductDetailRecord

    fun updateProduct(
        productId: Int,
        update: ProductUpdate,
        replaceOptions: List<NewProductOption>?,
        replaceImages: List<NewProductImage>?,
    ): ProductDetailRecord

    fun softDeleteProduct(productId: Int)

    fun createOption(
        productId: Int,
        newOption: NewProductOption,
    ): ProductOptionRecord

    fun updateOption(
        productId: Int,
        optionId: Int,
        update: ProductOptionUpdate,
    ): ProductOptionRecord

    fun deleteOption(
        productId: Int,
        optionId: Int,
    )

    fun createImage(
        productId: Int,
        newImage: NewProductImage,
    ): ProductImageRecord

    fun deleteImage(
        productId: Int,
        imageId: Int,
    )
}
