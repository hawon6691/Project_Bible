package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.matching

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class MatchingService(
    private val repository: MatchingRepository,
) {
    fun pending(page: Int, limit: Int): StubResponse {
        val normalizedPage = normalizePage(page)
        val normalizedLimit = normalizeLimit(limit)
        val result = repository.listPending(normalizedPage, normalizedLimit)
        return StubResponse(data = result.items.map(::mappingPayload), meta = pageMeta(normalizedPage, normalizedLimit, result.totalCount))
    }

    fun approve(mappingId: Int, productId: Int, adminUserId: Int): StubResponse {
        val mapping = requirePending(mappingId)
        if (!repository.productExists(productId)) {
            throw PbShopException(HttpStatusCode.NotFound, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.")
        }
        val updated = repository.approve(mapping.id, productId, adminUserId)
        return StubResponse(data = mappingPayload(updated))
    }

    fun reject(mappingId: Int, reason: String, adminUserId: Int): StubResponse {
        val mapping = requirePending(mappingId)
        if (reason.trim().isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "reason이 필요합니다.")
        }
        val updated = repository.reject(mapping.id, reason.trim(), adminUserId)
        return StubResponse(data = mappingPayload(updated))
    }

    fun autoMatch(adminUserId: Int): StubResponse = StubResponse(data = repository.autoMatch(adminUserId))

    fun stats(): StubResponse {
        val stats = repository.stats()
        return StubResponse(data = mapOf("pending" to stats.pending, "approved" to stats.approved, "rejected" to stats.rejected, "total" to stats.total))
    }

    private fun requirePending(mappingId: Int): ProductMappingRecord {
        val mapping =
            repository.findById(mappingId)
                ?: throw PbShopException(HttpStatusCode.NotFound, "RESOURCE_NOT_FOUND", "매핑 대기 건을 찾을 수 없습니다.")
        if (mapping.status != ProductMappingStatus.PENDING) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "대기 상태 매핑만 처리할 수 있습니다.")
        }
        return mapping
    }

    private fun mappingPayload(record: ProductMappingRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "sourceName" to record.sourceName,
            "sourceBrand" to record.sourceBrand,
            "sourceSeller" to record.sourceSeller,
            "sourceUrl" to record.sourceUrl,
            "productId" to record.productId,
            "status" to record.status.name,
            "confidence" to record.confidence,
            "reason" to record.reason,
            "reviewedBy" to record.reviewedBy,
            "reviewedAt" to record.reviewedAt?.toString(),
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun normalizePage(page: Int): Int = if (page > 0) page else 1

    private fun normalizeLimit(limit: Int): Int = limit.coerceIn(1, 100).takeIf { it > 0 } ?: 20

    private fun pageMeta(page: Int, limit: Int, totalCount: Int): Map<String, Int> =
        mapOf(
            "page" to page,
            "limit" to limit,
            "totalCount" to totalCount,
            "totalPages" to if (totalCount == 0) 0 else ((totalCount - 1) / limit) + 1,
        )
}
