package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.fraud

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class FraudService(
    private val repository: FraudRepository,
) {
    fun alerts(status: String?, page: Int, limit: Int): StubResponse {
        val normalizedPage = if (page > 0) page else 1
        val normalizedLimit = limit.coerceIn(1, 100).takeIf { it > 0 } ?: 20
        val result = repository.listAlerts(parseStatus(status), normalizedPage, normalizedLimit)
        return StubResponse(
            data = result.items.map(::alertPayload),
            meta = mapOf(
                "page" to normalizedPage,
                "limit" to normalizedLimit,
                "totalCount" to result.totalCount,
                "totalPages" to if (result.totalCount == 0) 0 else ((result.totalCount - 1) / normalizedLimit) + 1,
            ),
        )
    }

    fun approve(id: Int, adminUserId: Int): StubResponse {
        requireAlert(id)
        repository.approveAlert(id, adminUserId)
        return StubResponse(data = mapOf("message" to "이상 가격 알림이 승인되었습니다."))
    }

    fun reject(id: Int, adminUserId: Int): StubResponse {
        requireAlert(id)
        repository.rejectAlert(id, adminUserId)
        return StubResponse(data = mapOf("message" to "이상 가격 알림이 거절되었습니다."))
    }

    fun realPrice(productId: Int, sellerId: Int?): StubResponse {
        if (productId <= 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "상품 ID가 올바르지 않습니다.")
        }
        val result =
            repository.findRealPrice(productId, sellerId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "가격 정보를 찾을 수 없습니다.")
        return StubResponse(
            data = mapOf(
                "productPrice" to result.productPrice,
                "shippingFee" to result.shippingFee,
                "totalPrice" to result.totalPrice,
                "shippingType" to result.shippingType,
            )
        )
    }

    private fun requireAlert(id: Int): FraudAlertRecord =
        repository.findAlertById(id)
            ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "이상 가격 알림을 찾을 수 없습니다.")

    private fun parseStatus(status: String?): FraudAlertStatus? =
        status?.trim()?.takeIf { it.isNotBlank() }?.uppercase()?.let {
            FraudAlertStatus.entries.firstOrNull { entry -> entry.name == it }
                ?: throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "지원하지 않는 fraud status 입니다.")
        }

    private fun alertPayload(record: FraudAlertRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "productId" to record.productId,
            "sellerId" to record.sellerId,
            "priceEntryId" to record.priceEntryId,
            "status" to record.status.name,
            "reason" to record.reason,
            "detectedPrice" to record.detectedPrice,
            "averagePrice" to record.averagePrice,
            "deviationPercent" to record.deviationPercent,
            "reviewedBy" to record.reviewedBy,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )
}
