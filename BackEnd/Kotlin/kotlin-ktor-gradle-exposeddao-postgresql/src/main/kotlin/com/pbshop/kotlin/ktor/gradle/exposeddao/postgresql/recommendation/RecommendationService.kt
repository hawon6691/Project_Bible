package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.recommendation

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.RecommendationType
import io.ktor.http.HttpStatusCode
import java.time.LocalDate

class RecommendationService(
    private val repository: RecommendationRepository,
) {
    fun today(): StubResponse =
        StubResponse(data = repository.listTodayRecommendations(20).map(::summaryPayload))

    fun personalized(
        userId: Int,
        limit: Int?,
    ): StubResponse = StubResponse(data = repository.listPersonalizedRecommendations(userId, (limit ?: 20).coerceIn(1, 100)).map(::summaryPayload))

    fun adminList(): StubResponse =
        StubResponse(
            data =
                repository.listAdminRecommendations().map {
                    mapOf(
                        "id" to it.id,
                        "productId" to it.productId,
                        "productName" to it.productName,
                        "type" to it.type.name,
                        "sortOrder" to it.sortOrder,
                        "startDate" to it.startDate?.toString(),
                        "endDate" to it.endDate?.toString(),
                        "createdAt" to it.createdAt.toString(),
                    )
                },
        )

    fun create(request: RecommendationCreateRequest): StubResponse {
        if (!repository.productExists(request.productId)) {
            throw PbShopException(HttpStatusCode.BadRequest, "PRODUCT_NOT_FOUND", "추천할 상품을 찾을 수 없습니다.")
        }
        val created =
            repository.createRecommendation(
                NewRecommendation(
                    productId = request.productId,
                    type = parseType(request.type),
                    sortOrder = request.sortOrder.coerceAtLeast(0),
                    startDate = request.startDate?.let(LocalDate::parse),
                    endDate = request.endDate?.let(LocalDate::parse),
                ),
            )
        return StubResponse(
            status = HttpStatusCode.Created,
            data =
                mapOf(
                    "id" to created.id,
                    "productId" to created.productId,
                    "type" to created.type.name,
                    "sortOrder" to created.sortOrder,
                    "startDate" to created.startDate?.toString(),
                    "endDate" to created.endDate?.toString(),
                ),
        )
    }

    fun delete(id: Int): StubResponse {
        repository.deleteRecommendation(id)
        return StubResponse(data = mapOf("message" to "Recommendation deleted."))
    }

    private fun parseType(value: String): RecommendationType =
        runCatching { RecommendationType.valueOf(value.trim().uppercase()) }
            .getOrElse {
                throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "유효하지 않은 recommendation type 입니다.")
            }

    private fun summaryPayload(record: RecommendationRecord): Map<String, Any?> =
        mapOf(
            "id" to record.productId,
            "productId" to record.productId,
            "name" to record.productName,
            "thumbnailUrl" to record.thumbnailUrl,
            "lowestPrice" to record.lowestPrice,
        )
}
