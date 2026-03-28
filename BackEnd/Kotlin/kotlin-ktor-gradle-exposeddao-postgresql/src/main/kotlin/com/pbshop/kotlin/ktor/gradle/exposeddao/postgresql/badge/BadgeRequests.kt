package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.badge

import kotlinx.serialization.Serializable

@Serializable
data class BadgeConditionRequest(
    val metric: String,
    val threshold: Int,
) {
    fun toRecord(): BadgeCondition = BadgeCondition(metric = metric.trim(), threshold = threshold)
}

@Serializable
data class CreateBadgeRequest(
    val name: String,
    val description: String,
    val iconUrl: String,
    val type: String,
    val condition: BadgeConditionRequest? = null,
    val rarity: String,
)

@Serializable
data class UpdateBadgeRequest(
    val name: String? = null,
    val description: String? = null,
    val iconUrl: String? = null,
    val type: String? = null,
    val condition: BadgeConditionRequest? = null,
    val rarity: String? = null,
)

@Serializable
data class GrantBadgeRequest(
    val userId: Int,
    val reason: String? = null,
)
