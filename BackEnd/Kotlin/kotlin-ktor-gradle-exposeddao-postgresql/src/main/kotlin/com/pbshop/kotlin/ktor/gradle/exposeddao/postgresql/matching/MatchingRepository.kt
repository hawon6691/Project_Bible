package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.matching

import java.time.Instant

enum class ProductMappingStatus {
    PENDING,
    APPROVED,
    REJECTED,
}

data class ProductMappingRecord(
    val id: Int,
    val sourceName: String,
    val sourceBrand: String?,
    val sourceSeller: String?,
    val sourceUrl: String?,
    val productId: Int?,
    val status: ProductMappingStatus,
    val confidence: Double,
    val reason: String?,
    val reviewedBy: Int?,
    val reviewedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ProductMappingPageResult(
    val items: List<ProductMappingRecord>,
    val totalCount: Int,
)

data class ProductMappingStats(
    val pending: Int,
    val approved: Int,
    val rejected: Int,
    val total: Int,
)

interface MatchingRepository {
    fun listPending(page: Int, limit: Int): ProductMappingPageResult

    fun findById(id: Int): ProductMappingRecord?

    fun productExists(productId: Int): Boolean

    fun approve(id: Int, productId: Int, reviewedBy: Int): ProductMappingRecord

    fun reject(id: Int, reason: String, reviewedBy: Int): ProductMappingRecord

    fun autoMatch(reviewedBy: Int): Map<String, Int>

    fun stats(): ProductMappingStats
}
