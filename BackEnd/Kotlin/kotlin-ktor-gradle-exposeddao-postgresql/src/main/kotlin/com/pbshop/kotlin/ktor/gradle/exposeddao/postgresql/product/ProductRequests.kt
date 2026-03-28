package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product

import kotlinx.serialization.Serializable

@Serializable
data class ProductQueryRequest(
    val page: Int = 1,
    val limit: Int = 20,
    val categoryId: Int? = null,
    val search: String? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val sort: String? = null,
    val specs: String? = null,
)

@Serializable
data class ProductOptionRequest(
    val name: String,
    val values: List<String>,
)

@Serializable
data class ProductImageRequest(
    val url: String,
    val isMain: Boolean = false,
    val sortOrder: Int = 0,
)

@Serializable
data class ProductCreateRequest(
    val name: String,
    val description: String,
    val price: Int,
    val discountPrice: Int? = null,
    val stock: Int,
    val status: String,
    val categoryId: Int,
    val thumbnailUrl: String? = null,
    val options: List<ProductOptionRequest> = emptyList(),
    val images: List<ProductImageRequest> = emptyList(),
)

@Serializable
data class ProductUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val price: Int? = null,
    val discountPrice: Int? = null,
    val stock: Int? = null,
    val status: String? = null,
    val categoryId: Int? = null,
    val thumbnailUrl: String? = null,
    val options: List<ProductOptionRequest>? = null,
    val images: List<ProductImageRequest>? = null,
)

data class ProductImageUploadRequest(
    val fileName: String,
    val mimeType: String,
    val size: Int,
    val uploadedByUserId: Int?,
    val isMain: Boolean,
    val sortOrder: Int,
)
