package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.price

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class PriceService(
    private val repository: PriceRepository,
) {
    fun listProductPrices(productId: Int): StubResponse {
        ensureProductExists(productId)
        return StubResponse(data = repository.listProductPrices(productId).map(::priceEntryPayload))
    }

    fun createPriceEntry(
        productId: Int,
        request: PriceEntryRequest,
    ): StubResponse {
        ensureProductExists(productId)
        validateRequest(request)
        ensureSellerExists(request.sellerId)
        val created =
            repository.createPriceEntry(
                productId,
                NewPriceEntry(
                    sellerId = request.sellerId,
                    price = request.price,
                    shippingCost = request.shippingCost,
                    shippingInfo = request.shippingInfo?.trim()?.takeIf { it.isNotBlank() },
                    productUrl = request.productUrl.trim(),
                    shippingFee = request.shippingFee,
                    shippingType = normalizeShippingType(request.shippingType),
                    isAvailable = request.isAvailable,
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = priceEntryPayload(created))
    }

    fun updatePriceEntry(
        id: Int,
        request: PriceEntryUpdateRequest,
    ): StubResponse {
        if (
            request.sellerId == null &&
            request.price == null &&
            request.shippingCost == null &&
            request.shippingInfo == null &&
            request.productUrl == null &&
            request.shippingFee == null &&
            request.shippingType == null &&
            request.isAvailable == null
        ) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "수정할 가격 값이 없습니다.")
        }
        request.sellerId?.let(::ensureSellerExists)
        request.price?.let(::validateNonNegativePrice)
        request.shippingCost?.let(::validateNonNegativeAmount)
        request.shippingFee?.let(::validateNonNegativeAmount)
        request.productUrl?.let(::validateUrl)
        request.shippingType?.let(::normalizeShippingType)
        val updated =
            repository.updatePriceEntry(
                id,
                PriceEntryUpdate(
                    sellerId = request.sellerId,
                    price = request.price,
                    shippingCost = request.shippingCost,
                    shippingInfo = request.shippingInfo?.trim(),
                    productUrl = request.productUrl?.trim(),
                    shippingFee = request.shippingFee,
                    shippingType = request.shippingType?.let(::normalizeShippingType),
                    isAvailable = request.isAvailable,
                ),
            )
        return StubResponse(data = priceEntryPayload(updated))
    }

    fun deletePriceEntry(id: Int): StubResponse {
        repository.findPriceEntryById(id)
            ?: throw PbShopException(HttpStatusCode.NotFound, "PRICE_ENTRY_NOT_FOUND", "가격 정보를 찾을 수 없습니다.")
        repository.deletePriceEntry(id)
        return StubResponse(data = mapOf("message" to "가격 정보가 삭제되었습니다."))
    }

    fun priceHistory(productId: Int): StubResponse {
        ensureProductExists(productId)
        return StubResponse(
            data =
                repository.listPriceHistory(productId).map {
                    mapOf(
                        "date" to it.date.toString(),
                        "lowestPrice" to it.lowestPrice,
                        "averagePrice" to it.averagePrice,
                        "highestPrice" to it.highestPrice,
                    )
                },
        )
    }

    fun listAlerts(
        userId: Int,
        page: Int,
        limit: Int,
    ): StubResponse {
        val normalizedPage = if (page < 1) 1 else page
        val normalizedLimit = limit.coerceIn(1, 100)
        val result = repository.listPriceAlerts(userId, normalizedPage, normalizedLimit)
        return StubResponse(
            data = result.items.map(::priceAlertPayload),
            meta =
                mapOf(
                    "page" to normalizedPage,
                    "limit" to normalizedLimit,
                    "totalCount" to result.totalCount,
                    "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount + normalizedLimit - 1) / normalizedLimit),
                ),
        )
    }

    fun createAlert(
        userId: Int,
        request: PriceAlertRequest,
    ): StubResponse {
        ensureProductExists(request.productId)
        validateNonNegativePrice(request.targetPrice)
        val created = repository.createPriceAlert(userId, NewPriceAlert(request.productId, request.targetPrice, request.isActive))
        return StubResponse(status = HttpStatusCode.Created, data = priceAlertPayload(created))
    }

    private fun ensureProductExists(productId: Int) {
        if (!repository.productExists(productId)) {
            throw PbShopException(HttpStatusCode.NotFound, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.")
        }
    }

    private fun ensureSellerExists(sellerId: Int) {
        if (!repository.sellerExists(sellerId)) {
            throw PbShopException(HttpStatusCode.BadRequest, "SELLER_NOT_FOUND", "판매처를 찾을 수 없습니다.")
        }
    }

    private fun validateRequest(request: PriceEntryRequest) {
        validateNonNegativePrice(request.price)
        validateNonNegativeAmount(request.shippingCost)
        validateNonNegativeAmount(request.shippingFee)
        validateUrl(request.productUrl)
        normalizeShippingType(request.shippingType)
    }

    private fun validateNonNegativePrice(value: Int) {
        if (value < 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "price는 0 이상이어야 합니다.")
        }
    }

    private fun validateNonNegativeAmount(value: Int) {
        if (value < 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "금액은 0 이상이어야 합니다.")
        }
    }

    private fun validateUrl(url: String) {
        if (url.trim().isBlank() || !url.trim().startsWith("http")) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효한 상품 URL이 필요합니다.")
        }
    }

    private fun normalizeShippingType(value: String): String {
        val normalized = value.trim().uppercase()
        if (normalized !in SHIPPING_TYPES) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효하지 않은 배송 타입입니다.")
        }
        return normalized
    }

    private fun priceEntryPayload(record: PriceEntryRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "productId" to record.productId,
            "productName" to record.productName,
            "seller" to
                mapOf(
                    "id" to record.sellerId,
                    "name" to record.sellerName,
                    "logoUrl" to record.sellerLogoUrl,
                    "trustScore" to record.trustScore,
                ),
            "price" to record.price,
            "shippingCost" to record.shippingCost,
            "shippingInfo" to record.shippingInfo,
            "productUrl" to record.productUrl,
            "shippingFee" to record.shippingFee,
            "shippingType" to record.shippingType,
            "totalPrice" to record.totalPrice,
            "clickCount" to record.clickCount,
            "isAvailable" to record.isAvailable,
            "crawledAt" to record.crawledAt?.toString(),
        )

    private fun priceAlertPayload(record: PriceAlertRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "productId" to record.productId,
            "productName" to record.productName,
            "targetPrice" to record.targetPrice,
            "isTriggered" to record.isTriggered,
            "triggeredAt" to record.triggeredAt?.toString(),
            "isActive" to record.isActive,
            "createdAt" to record.createdAt.toString(),
        )

    companion object {
        private val SHIPPING_TYPES = setOf("FREE", "PAID", "CONDITIONAL")
    }
}
