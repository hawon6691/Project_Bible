package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.recommendation

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.RecommendationType
import java.time.Instant
import java.time.LocalDate

class InMemoryRecommendationRepository private constructor(
    private val items: MutableList<RecommendationRecord>,
) : RecommendationRepository {
    private var nextId = (items.maxOfOrNull { it.id } ?: 0) + 1

    override fun listTodayRecommendations(limit: Int): List<RecommendationRecord> =
        items.filter { it.type == RecommendationType.TODAY }.sortedBy { it.sortOrder }.take(limit)

    override fun listPersonalizedRecommendations(
        userId: Int,
        limit: Int,
    ): List<RecommendationRecord> = items.sortedBy { it.sortOrder }.take(limit)

    override fun listAdminRecommendations(): List<RecommendationRecord> = items.sortedBy { it.sortOrder }

    override fun createRecommendation(newRecommendation: NewRecommendation): RecommendationRecord {
        val productName =
            when (newRecommendation.productId) {
                1 -> "게이밍 노트북 A15"
                2 -> "크리에이터북 16"
                else -> "추천 상품 ${newRecommendation.productId}"
            }
        val created =
            RecommendationRecord(
                id = nextId++,
                productId = newRecommendation.productId,
                productName = productName,
                thumbnailUrl = "https://img.example.com/products/${newRecommendation.productId}-thumb.jpg",
                lowestPrice = 990000 + (newRecommendation.productId * 10000),
                type = newRecommendation.type,
                sortOrder = newRecommendation.sortOrder,
                startDate = newRecommendation.startDate,
                endDate = newRecommendation.endDate,
                createdAt = Instant.now(),
            )
        items += created
        return created
    }

    override fun deleteRecommendation(id: Int) {
        items.removeIf { it.id == id }
    }

    override fun productExists(productId: Int): Boolean = productId in setOf(1, 2, 3, 4, 5)

    companion object {
        fun seeded(): InMemoryRecommendationRepository =
            InMemoryRecommendationRepository(
                mutableListOf(
                    RecommendationRecord(1, 1, "게이밍 노트북 A15", "https://img.example.com/products/1-thumb.jpg", 1490000, RecommendationType.TODAY, 1, LocalDate.now().minusDays(1), LocalDate.now().plusDays(7), Instant.now().minusSeconds(86_400)),
                    RecommendationRecord(2, 2, "크리에이터북 16", "https://img.example.com/products/2-thumb.jpg", 1890000, RecommendationType.EDITOR_PICK, 2, null, null, Instant.now().minusSeconds(43_200)),
                ),
            )
    }
}
