package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.compare

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode

class CompareService(
    private val repository: CompareRepository,
) {
    fun add(compareKey: String, productId: Int): StubResponse {
        if (productId <= 0) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "productId가 필요합니다.")
        }
        if (!repository.productExists(productId)) {
            throw PbShopException(HttpStatusCode.NotFound, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다.")
        }
        val current = repository.list(compareKey)
        if (current.none { it.productId == productId } && current.size >= 4) {
            throw PbShopException(HttpStatusCode.BadRequest, "COMPARE_LIMIT_EXCEEDED", "비교함에는 최대 4개 상품만 담을 수 있습니다.")
        }
        repository.add(compareKey, productId)
        return StubResponse(data = mapOf("compareList" to repository.list(compareKey).map(::itemPayload)))
    }

    fun remove(compareKey: String, productId: Int): StubResponse {
        repository.remove(compareKey, productId)
        return StubResponse(data = mapOf("compareList" to repository.list(compareKey).map(::itemPayload)))
    }

    fun list(compareKey: String): StubResponse =
        StubResponse(data = mapOf("compareList" to repository.list(compareKey).map(::itemPayload)))

    fun detail(compareKey: String): StubResponse {
        val items = repository.detail(compareKey)
        val specNames = items.flatMap { it.specs.keys }.distinct().sorted()
        val diff =
            specNames.map { name ->
                mapOf(
                    "name" to name,
                    "values" to items.map { it.productId to it.specs[name] }.toMap(),
                    "allSame" to (items.map { it.specs[name] }.distinct().size <= 1),
                )
            }
        return StubResponse(
            data =
                mapOf(
                    "compareList" to items.map(::detailPayload),
                    "diff" to diff,
                )
        )
    }

    private fun itemPayload(record: CompareItemRecord): Map<String, Any?> =
        mapOf(
            "productId" to record.productId,
            "name" to record.name,
            "slug" to record.slug,
            "thumbnailUrl" to record.thumbnailUrl,
        )

    private fun detailPayload(record: CompareDetailItemRecord): Map<String, Any?> =
        mapOf(
            "productId" to record.productId,
            "name" to record.name,
            "slug" to record.slug,
            "categoryId" to record.categoryId,
            "bestPrice" to record.bestPrice,
            "ratingAvg" to record.ratingAvg,
            "specs" to record.specs,
        )
}
