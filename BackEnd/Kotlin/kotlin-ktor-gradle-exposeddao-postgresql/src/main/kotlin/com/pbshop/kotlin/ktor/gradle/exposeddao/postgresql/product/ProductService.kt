package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.product

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.ProductStatus
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class ProductService(
    private val repository: ProductRepository,
) {
    fun listProducts(request: ProductQueryRequest): StubResponse {
        val page = if (request.page < 1) 1 else request.page
        val limit = request.limit.coerceIn(1, 100)
        val query =
            ProductListQuery(
                page = page,
                limit = limit,
                categoryId = request.categoryId,
                search = request.search?.trim()?.takeIf { it.isNotBlank() },
                minPrice = request.minPrice,
                maxPrice = request.maxPrice,
                sort = parseSort(request.sort),
                specFilters = parseSpecFilters(request.specs),
            )
        validateListQuery(query)

        val result = repository.listProducts(query)
        return StubResponse(
            data = result.items.map(::summaryPayload),
            meta =
                mapOf(
                    "page" to query.page,
                    "limit" to query.limit,
                    "totalCount" to result.totalCount,
                    "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount + query.limit - 1) / query.limit),
                ),
        )
    }

    fun detail(productId: Int): StubResponse =
        StubResponse(data = detailPayload(requireProductDetail(productId)))

    fun create(request: ProductCreateRequest): StubResponse {
        validateProductCreateRequest(request)
        ensureCategoryExists(request.categoryId)

        val created =
            repository.createProduct(
                newProduct =
                    NewProduct(
                        name = request.name.trim(),
                        description = request.description.trim(),
                        price = request.price,
                        discountPrice = request.discountPrice,
                        stock = request.stock,
                        status = parseStatus(request.status),
                        categoryId = request.categoryId,
                        thumbnailUrl = request.thumbnailUrl?.trim()?.takeIf { it.isNotBlank() },
                    ),
                options = request.options.map(::toNewOption),
                images = normalizeImages(request.images.map(::toNewImage)),
            )

        return StubResponse(status = HttpStatusCode.Created, data = detailPayload(created))
    }

    fun update(
        productId: Int,
        request: ProductUpdateRequest,
    ): StubResponse {
        if (
            request.name == null &&
            request.description == null &&
            request.price == null &&
            request.discountPrice == null &&
            request.stock == null &&
            request.status == null &&
            request.categoryId == null &&
            request.thumbnailUrl == null &&
            request.options == null &&
            request.images == null
        ) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "수정할 값이 없습니다.")
        }

        request.categoryId?.let(::ensureCategoryExists)
        validateProductUpdateRequest(request)

        val updated =
            repository.updateProduct(
                productId = productId,
                update =
                    ProductUpdate(
                        name = request.name?.trim(),
                        description = request.description?.trim(),
                        price = request.price,
                        discountPrice = request.discountPrice,
                        stock = request.stock,
                        status = request.status?.let(::parseStatus),
                        categoryId = request.categoryId,
                        thumbnailUrl = request.thumbnailUrl?.trim(),
                    ),
                replaceOptions = request.options?.map(::toNewOption),
                replaceImages = request.images?.map(::toNewImage)?.let(::normalizeImages),
            )

        return StubResponse(data = detailPayload(updated))
    }

    fun delete(productId: Int): StubResponse {
        requireProduct(productId)
        repository.softDeleteProduct(productId)
        return StubResponse(data = mapOf("message" to "상품이 삭제되었습니다."))
    }

    fun addOption(
        productId: Int,
        request: ProductOptionRequest,
    ): StubResponse {
        requireProduct(productId)
        validateOptionRequest(request)
        val created = repository.createOption(productId, toNewOption(request))
        return StubResponse(status = HttpStatusCode.Created, data = optionPayload(created))
    }

    fun updateOption(
        productId: Int,
        optionId: Int,
        request: ProductOptionRequest,
    ): StubResponse {
        requireProduct(productId)
        validateOptionRequest(request)
        val updated =
            repository.updateOption(
                productId = productId,
                optionId = optionId,
                update = ProductOptionUpdate(name = request.name.trim(), values = request.values.map(String::trim).filter(String::isNotBlank)),
            )
        return StubResponse(data = optionPayload(updated))
    }

    fun deleteOption(
        productId: Int,
        optionId: Int,
    ): StubResponse {
        requireProduct(productId)
        repository.deleteOption(productId, optionId)
        return StubResponse(data = mapOf("message" to "옵션이 삭제되었습니다."))
    }

    fun addImage(
        productId: Int,
        request: ProductImageUploadRequest,
    ): StubResponse {
        requireProduct(productId)
        val sanitizedName = request.fileName.trim().ifBlank { "product-image.bin" }.replace("\\s+".toRegex(), "-")
        val image =
            repository.createImage(
                productId = productId,
                newImage =
                    NewProductImage(
                        url = "https://img.pbshop.dev/products/$productId/$sanitizedName",
                        isMain = request.isMain,
                        sortOrder = request.sortOrder.coerceAtLeast(0),
                    ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = imagePayload(image))
    }

    fun deleteImage(
        productId: Int,
        imageId: Int,
    ): StubResponse {
        requireProduct(productId)
        repository.deleteImage(productId, imageId)
        return StubResponse(data = mapOf("message" to "이미지가 삭제되었습니다."))
    }

    private fun requireProduct(productId: Int): ProductRecord =
        repository.findProductById(productId)
            ?.takeIf { it.deletedAt == null }
            ?: throw PbShopException(HttpStatusCode.NotFound, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.")

    private fun requireProductDetail(productId: Int): ProductDetailRecord =
        repository.findProductDetailById(productId)
            ?: throw PbShopException(HttpStatusCode.NotFound, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.")

    private fun ensureCategoryExists(categoryId: Int) {
        if (!repository.categoryExists(categoryId)) {
            throw PbShopException(HttpStatusCode.BadRequest, "CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다.")
        }
    }

    private fun validateListQuery(query: ProductListQuery) {
        if (query.minPrice != null && query.minPrice < 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "minPrice는 0 이상이어야 합니다.")
        }
        if (query.maxPrice != null && query.maxPrice < 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "maxPrice는 0 이상이어야 합니다.")
        }
        if (query.minPrice != null && query.maxPrice != null && query.minPrice > query.maxPrice) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "minPrice는 maxPrice보다 클 수 없습니다.")
        }
    }

    private fun validateProductCreateRequest(request: ProductCreateRequest) {
        validateName(request.name)
        validateDescription(request.description)
        validatePrice(request.price, request.discountPrice)
        validateStock(request.stock)
        parseStatus(request.status)
        request.options.forEach(::validateOptionRequest)
        request.images.forEach(::validateImageRequest)
    }

    private fun validateProductUpdateRequest(request: ProductUpdateRequest) {
        request.name?.let(::validateName)
        request.description?.let(::validateDescription)
        request.price?.let { validatePrice(it, request.discountPrice) }
        request.stock?.let(::validateStock)
        request.status?.let(::parseStatus)
        request.options?.forEach(::validateOptionRequest)
        request.images?.forEach(::validateImageRequest)
    }

    private fun validateName(value: String) {
        val normalized = value.trim()
        if (normalized.isBlank() || normalized.length > 200) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "상품명은 1자 이상 200자 이하로 입력해주세요.")
        }
    }

    private fun validateDescription(value: String) {
        if (value.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "상품 설명은 비어 있을 수 없습니다.")
        }
    }

    private fun validatePrice(
        price: Int,
        discountPrice: Int?,
    ) {
        if (price < 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "price는 0 이상이어야 합니다.")
        }
        if (discountPrice != null && discountPrice < 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "discountPrice는 0 이상이어야 합니다.")
        }
    }

    private fun validateStock(stock: Int) {
        if (stock < 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "stock은 0 이상이어야 합니다.")
        }
    }

    private fun validateOptionRequest(request: ProductOptionRequest) {
        if (request.name.trim().isBlank() || request.name.trim().length > 50) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "옵션명은 1자 이상 50자 이하로 입력해주세요.")
        }
        if (request.values.map(String::trim).none(String::isNotBlank)) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "옵션 값은 최소 1개 이상 필요합니다.")
        }
    }

    private fun validateImageRequest(request: ProductImageRequest) {
        if (request.url.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "이미지 URL은 비어 있을 수 없습니다.")
        }
        if (request.sortOrder < 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "sortOrder는 0 이상이어야 합니다.")
        }
    }

    private fun parseSort(value: String?): ProductSort =
        if (value.isNullOrBlank()) {
            ProductSort.NEWEST
        } else {
            ProductSort.entries.firstOrNull { it.apiValue == value.trim().lowercase() }
                ?: throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효하지 않은 상품 정렬 값입니다.")
        }

    private fun parseStatus(value: String): ProductStatus =
        runCatching { ProductStatus.valueOf(value.trim().uppercase()) }
            .getOrElse {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효하지 않은 상품 상태입니다.")
            }

    private fun parseSpecFilters(specs: String?): Map<String, String> {
        if (specs.isNullOrBlank()) {
            return emptyMap()
        }
        return runCatching {
            val parsed = Json.parseToJsonElement(specs)
            if (parsed !is JsonObject) {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "specs는 JSON object 형식이어야 합니다.")
            }
            parsed.entries.associate { (key, value) -> key to value.jsonPrimitive.content }
        }.getOrElse {
            if (it is PbShopException) throw it
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "specs 파라미터 형식이 올바르지 않습니다.")
        }
    }

    private fun toNewOption(request: ProductOptionRequest): NewProductOption =
        NewProductOption(request.name.trim(), request.values.map(String::trim).filter(String::isNotBlank))

    private fun toNewImage(request: ProductImageRequest): NewProductImage =
        NewProductImage(request.url.trim(), request.isMain, request.sortOrder.coerceAtLeast(0))

    private fun normalizeImages(images: List<NewProductImage>): List<NewProductImage> {
        if (images.isEmpty()) return images
        val firstMainIndex = images.indexOfFirst { it.isMain }
        if (firstMainIndex == -1) return images
        return images.mapIndexed { index, image -> image.copy(isMain = index == firstMainIndex) }
    }

    private fun summaryPayload(record: ProductRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "name" to record.name,
            "lowestPrice" to (record.lowestPrice ?: record.price),
            "sellerCount" to record.sellerCount,
            "thumbnailUrl" to record.thumbnailUrl,
            "reviewCount" to record.reviewCount,
            "averageRating" to record.averageRating,
            "createdAt" to record.createdAt.toString(),
        )

    private fun detailPayload(detail: ProductDetailRecord): Map<String, Any?> =
        mapOf(
            "id" to detail.product.id,
            "name" to detail.product.name,
            "description" to detail.product.description,
            "lowestPrice" to (detail.product.lowestPrice ?: detail.product.price),
            "highestPrice" to detail.highestPrice,
            "averagePrice" to detail.averagePrice,
            "stock" to detail.product.stock,
            "status" to detail.product.status.name,
            "category" to mapOf("id" to detail.product.categoryId, "name" to detail.product.categoryName),
            "options" to detail.options.map(::optionPayload),
            "images" to detail.images.map(::imagePayload),
            "specs" to detail.specs.map { mapOf("name" to it.name, "value" to it.value) },
            "priceEntries" to
                detail.priceEntries.map {
                    mapOf(
                        "seller" to mapOf("id" to it.sellerId, "name" to it.sellerName, "logoUrl" to it.sellerLogoUrl, "trustScore" to it.trustScore),
                        "price" to it.price,
                        "url" to it.url,
                        "shipping" to it.shipping,
                    )
                },
            "reviewCount" to detail.product.reviewCount,
            "averageRating" to detail.product.averageRating,
            "createdAt" to detail.product.createdAt.toString(),
        )

    private fun optionPayload(record: ProductOptionRecord): Map<String, Any?> =
        mapOf("id" to record.id, "name" to record.name, "values" to record.values)

    private fun imagePayload(record: ProductImageRecord): Map<String, Any?> =
        mapOf("id" to record.id, "url" to record.url, "isMain" to record.isMain, "sortOrder" to record.sortOrder)
}
