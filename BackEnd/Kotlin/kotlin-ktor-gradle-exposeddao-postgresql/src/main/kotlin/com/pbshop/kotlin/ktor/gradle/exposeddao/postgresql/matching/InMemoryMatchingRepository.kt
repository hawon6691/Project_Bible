package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.matching

import java.time.Instant

class InMemoryMatchingRepository(
    mappings: List<ProductMappingRecord>,
    private val productIds: Set<Int>,
) : MatchingRepository {
    private val mappings = linkedMapOf<Int, ProductMappingRecord>()

    init {
        mappings.forEach { this.mappings[it.id] = it }
    }

    override fun listPending(page: Int, limit: Int): ProductMappingPageResult {
        val filtered = mappings.values.filter { it.status == ProductMappingStatus.PENDING }.sortedByDescending { it.createdAt }
        val fromIndex = ((page - 1) * limit).coerceAtMost(filtered.size)
        val toIndex = (fromIndex + limit).coerceAtMost(filtered.size)
        return ProductMappingPageResult(filtered.subList(fromIndex, toIndex), filtered.size)
    }

    override fun findById(id: Int): ProductMappingRecord? = mappings[id]

    override fun productExists(productId: Int): Boolean = productIds.contains(productId)

    override fun approve(id: Int, productId: Int, reviewedBy: Int): ProductMappingRecord {
        val updated =
            mappings[id]!!.copy(
                status = ProductMappingStatus.APPROVED,
                productId = productId,
                reason = null,
                reviewedBy = reviewedBy,
                reviewedAt = Instant.now(),
                updatedAt = Instant.now(),
            )
        mappings[id] = updated
        return updated
    }

    override fun reject(id: Int, reason: String, reviewedBy: Int): ProductMappingRecord {
        val updated =
            mappings[id]!!.copy(
                status = ProductMappingStatus.REJECTED,
                reason = reason,
                reviewedBy = reviewedBy,
                reviewedAt = Instant.now(),
                updatedAt = Instant.now(),
            )
        mappings[id] = updated
        return updated
    }

    override fun autoMatch(reviewedBy: Int): Map<String, Int> {
        var matchedCount = 0
        mappings.replaceAll { _, value ->
            if (value.status == ProductMappingStatus.PENDING && value.sourceName.contains("노트북")) {
                matchedCount += 1
                value.copy(
                    status = ProductMappingStatus.APPROVED,
                    productId = value.productId ?: productIds.firstOrNull(),
                    confidence = 75.0,
                    reason = null,
                    reviewedBy = reviewedBy,
                    reviewedAt = Instant.now(),
                    updatedAt = Instant.now(),
                )
            } else {
                value
            }
        }
        val pendingCount = mappings.values.count { it.status == ProductMappingStatus.PENDING }
        return mapOf("matchedCount" to matchedCount, "pendingCount" to pendingCount)
    }

    override fun stats(): ProductMappingStats {
        val pending = mappings.values.count { it.status == ProductMappingStatus.PENDING }
        val approved = mappings.values.count { it.status == ProductMappingStatus.APPROVED }
        val rejected = mappings.values.count { it.status == ProductMappingStatus.REJECTED }
        return ProductMappingStats(pending, approved, rejected, mappings.size)
    }

    companion object {
        fun seeded(): InMemoryMatchingRepository {
            val now = Instant.parse("2026-03-20T12:00:00Z")
            return InMemoryMatchingRepository(
                mappings =
                    listOf(
                        ProductMappingRecord(1, "게이밍 노트북 A15 외부몰", "PB", "외부몰A", "https://seller-a.example.com/1", null, ProductMappingStatus.PENDING, 62.5, null, null, null, now, now),
                        ProductMappingRecord(2, "게이밍 노트북 B17", "PB", "외부몰B", "https://seller-b.example.com/2", 2, ProductMappingStatus.APPROVED, 95.0, null, 1, now, now, now),
                        ProductMappingRecord(3, "구형 태블릿", "PB", "외부몰C", "https://seller-c.example.com/3", null, ProductMappingStatus.REJECTED, 10.0, "중복 상품", 1, now, now, now),
                    ),
                productIds = setOf(1, 2, 3),
            )
        }
    }
}
