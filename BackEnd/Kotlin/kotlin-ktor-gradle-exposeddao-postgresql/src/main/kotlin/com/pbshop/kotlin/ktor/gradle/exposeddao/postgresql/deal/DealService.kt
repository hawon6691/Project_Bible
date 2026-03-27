package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.deal

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode
import java.time.Instant

class DealService(
    private val repository: DealRepository,
) {
    fun list(
        type: String?,
        page: Int?,
        limit: Int?,
    ): StubResponse {
        val queryPage = (page ?: 1).coerceAtLeast(1)
        val queryLimit = (limit ?: 20).coerceIn(1, 100)
        val result = repository.listDeals(type?.trim()?.takeIf { it.isNotBlank() }, queryPage, queryLimit)
        return StubResponse(
            data = result.items.map(::summaryPayload),
            meta = pageMeta(queryPage, queryLimit, result.totalCount),
        )
    }

    fun detail(id: Int): StubResponse =
        StubResponse(data = detailPayload(requireDeal(id)))

    fun create(request: DealCreateRequest): StubResponse {
        validateCreate(request)
        request.products.forEach { ensureProductExists(it.productId) }
        val primaryProductId = request.products.first().productId
        val created =
            repository.createDeal(
                NewDeal(
                    productId = primaryProductId,
                    title = request.title.trim(),
                    description = request.description?.trim(),
                    type = request.type.trim().uppercase(),
                    discountRate = request.discountRate,
                    startAt = Instant.parse(request.startDate),
                    endAt = Instant.parse(request.endDate),
                    isActive = true,
                    bannerUrl = request.bannerUrl?.trim(),
                    products = request.products.map { NewDealProduct(it.productId, it.dealPrice, it.stock) },
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = detailPayload(created))
    }

    fun update(
        id: Int,
        request: DealUpdateRequest,
    ): StubResponse {
        request.products?.forEach { ensureProductExists(it.productId) }
        val updated =
            repository.updateDeal(
                id,
                DealUpdate(
                    title = request.title?.trim(),
                    description = request.description?.trim(),
                    type = request.type?.trim()?.uppercase(),
                    discountRate = request.discountRate,
                    startAt = request.startDate?.let(Instant::parse),
                    endAt = request.endDate?.let(Instant::parse),
                    isActive = request.isActive,
                    bannerUrl = request.bannerUrl?.trim(),
                    products = request.products?.map { NewDealProduct(it.productId, it.dealPrice, it.stock) },
                ),
            )
        return StubResponse(data = detailPayload(updated))
    }

    fun delete(id: Int): StubResponse {
        repository.deleteDeal(id)
        return StubResponse(data = mapOf("message" to "Deal deleted."))
    }

    private fun validateCreate(request: DealCreateRequest) {
        if (request.title.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "특가 제목은 비어 있을 수 없습니다.")
        }
        if (request.products.isEmpty()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "특가 대상 상품이 필요합니다.")
        }
        request.products.forEach(::validateDealProduct)
    }

    private fun validateDealProduct(request: DealProductRequest) {
        if (request.dealPrice < 0 || request.stock < 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "dealPrice와 stock은 0 이상이어야 합니다.")
        }
    }

    private fun ensureProductExists(productId: Int) {
        if (!repository.productExists(productId)) {
            throw PbShopException(HttpStatusCode.BadRequest, "PRODUCT_NOT_FOUND", "특가 대상 상품을 찾을 수 없습니다.")
        }
    }

    private fun requireDeal(id: Int): DealRecord =
        repository.findDealById(id)
            ?: throw PbShopException(HttpStatusCode.NotFound, "DEAL_NOT_FOUND", "특가 정보를 찾을 수 없습니다.")

    private fun summaryPayload(record: DealRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "title" to record.title,
            "type" to record.type,
            "discountRate" to record.discountRate,
            "startAt" to record.startAt.toString(),
            "endAt" to record.endAt.toString(),
            "isActive" to record.isActive,
            "products" to record.products.map { mapOf("productId" to it.productId, "dealPrice" to it.dealPrice, "stock" to it.stock) },
        )

    private fun detailPayload(record: DealRecord): Map<String, Any?> =
        summaryPayload(record) + mapOf(
            "description" to record.description,
            "bannerUrl" to record.bannerUrl,
            "products" to
                record.products.map {
                    mapOf(
                        "id" to it.id,
                        "productId" to it.productId,
                        "productName" to it.productName,
                        "dealPrice" to it.dealPrice,
                        "stock" to it.stock,
                        "soldCount" to it.soldCount,
                    )
                },
        )

    private fun pageMeta(
        page: Int,
        limit: Int,
        totalCount: Int,
    ): Map<String, Int> =
        mapOf(
            "page" to page,
            "limit" to limit,
            "totalCount" to totalCount,
            "totalPages" to if (totalCount == 0) 0 else ((totalCount + limit - 1) / limit),
        )
}
