package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.badge

import java.time.Instant

enum class BadgeType {
    AUTO,
    MANUAL,
}

enum class BadgeRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY,
}

data class BadgeCondition(
    val metric: String,
    val threshold: Int,
)

data class BadgeRecord(
    val id: Int,
    val name: String,
    val description: String,
    val iconUrl: String,
    val type: BadgeType,
    val condition: BadgeCondition?,
    val rarity: BadgeRarity,
    val holderCount: Int,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class UserBadgeDetailRecord(
    val id: Int,
    val userId: Int,
    val badgeId: Int,
    val grantedByAdminId: Int?,
    val reason: String?,
    val grantedAt: Instant,
    val badge: BadgeRecord,
)

data class NewBadge(
    val name: String,
    val description: String,
    val iconUrl: String,
    val type: BadgeType,
    val condition: BadgeCondition?,
    val rarity: BadgeRarity,
)

data class BadgeUpdate(
    val name: String?,
    val description: String?,
    val iconUrl: String?,
    val type: BadgeType?,
    val condition: BadgeCondition?,
    val rarity: BadgeRarity?,
)

interface BadgeRepository {
    fun listBadges(): List<BadgeRecord>

    fun findBadgeById(id: Int): BadgeRecord?

    fun findBadgeByName(name: String): BadgeRecord?

    fun userExists(userId: Int): Boolean

    fun listUserBadges(userId: Int): List<UserBadgeDetailRecord>

    fun createBadge(newBadge: NewBadge): BadgeRecord

    fun updateBadge(
        badgeId: Int,
        update: BadgeUpdate,
    ): BadgeRecord

    fun deleteBadge(badgeId: Int): Boolean

    fun hasUserBadge(
        badgeId: Int,
        userId: Int,
    ): Boolean

    fun grantBadge(
        badgeId: Int,
        userId: Int,
        grantedByAdminId: Int?,
        reason: String?,
    ): UserBadgeDetailRecord

    fun revokeBadge(
        badgeId: Int,
        userId: Int,
    ): Boolean
}
