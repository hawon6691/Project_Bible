package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.recommendation

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.RecommendationType
import java.time.Instant
import java.time.LocalDate

data class RecommendationRecord(
    val id: Int,
    val productId: Int,
    val productName: String,
    val thumbnailUrl: String?,
    val lowestPrice: Int,
    val type: RecommendationType,
    val sortOrder: Int,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val createdAt: Instant,
)

data class NewRecommendation(
    val productId: Int,
    val type: RecommendationType,
    val sortOrder: Int,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
)

interface RecommendationRepository {
    fun listTodayRecommendations(limit: Int): List<RecommendationRecord>

    fun listPersonalizedRecommendations(
        userId: Int,
        limit: Int,
    ): List<RecommendationRecord>

    fun listAdminRecommendations(): List<RecommendationRecord>

    fun createRecommendation(newRecommendation: NewRecommendation): RecommendationRecord

    fun deleteRecommendation(id: Int)

    fun productExists(productId: Int): Boolean
}
