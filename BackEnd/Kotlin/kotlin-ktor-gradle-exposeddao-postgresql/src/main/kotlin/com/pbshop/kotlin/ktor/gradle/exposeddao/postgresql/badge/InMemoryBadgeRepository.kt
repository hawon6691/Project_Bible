package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.badge

import java.time.Instant

class InMemoryBadgeRepository(
    badges: List<BadgeRecord>,
    userBadges: List<UserBadgeDetailRecord>,
    userIds: Set<Int>,
) : BadgeRepository {
    private val badges = linkedMapOf<Int, BadgeRecord>()
    private val userBadges = linkedMapOf<Int, UserBadgeDetailRecord>()
    private val userIds = userIds.toMutableSet()
    private var badgeSequence = 1
    private var userBadgeSequence = 1

    init {
        badges.forEach {
            this.badges[it.id] = it
            badgeSequence = maxOf(badgeSequence, it.id + 1)
        }
        userBadges.forEach {
            this.userBadges[it.id] = it
            userBadgeSequence = maxOf(userBadgeSequence, it.id + 1)
        }
    }

    override fun listBadges(): List<BadgeRecord> = badges.values.filter { it.isActive }.sortedBy { it.id }

    override fun findBadgeById(id: Int): BadgeRecord? = badges[id]?.takeIf { it.isActive }

    override fun findBadgeByName(name: String): BadgeRecord? = badges.values.firstOrNull { it.isActive && it.name == name }

    override fun userExists(userId: Int): Boolean = userIds.contains(userId)

    override fun listUserBadges(userId: Int): List<UserBadgeDetailRecord> =
        userBadges.values.filter { it.userId == userId }.sortedByDescending { it.grantedAt }

    override fun createBadge(newBadge: NewBadge): BadgeRecord {
        val now = Instant.now()
        val saved =
            BadgeRecord(
                id = badgeSequence++,
                name = newBadge.name,
                description = newBadge.description,
                iconUrl = newBadge.iconUrl,
                type = newBadge.type,
                condition = newBadge.condition,
                rarity = newBadge.rarity,
                holderCount = 0,
                isActive = true,
                createdAt = now,
                updatedAt = now,
            )
        badges[saved.id] = saved
        return saved
    }

    override fun updateBadge(
        badgeId: Int,
        update: BadgeUpdate,
    ): BadgeRecord {
        val current = badges[badgeId] ?: error("Badge $badgeId not found")
        val updated =
            current.copy(
                name = update.name ?: current.name,
                description = update.description ?: current.description,
                iconUrl = update.iconUrl ?: current.iconUrl,
                type = update.type ?: current.type,
                condition = update.condition ?: current.condition,
                rarity = update.rarity ?: current.rarity,
                updatedAt = Instant.now(),
            )
        badges[badgeId] = updated
        userBadges.replaceAll { _, value -> if (value.badgeId == badgeId) value.copy(badge = updated) else value }
        return updated
    }

    override fun deleteBadge(badgeId: Int): Boolean {
        val current = badges[badgeId] ?: return false
        badges[badgeId] = current.copy(isActive = false, updatedAt = Instant.now(), holderCount = 0)
        userBadges.entries.removeIf { it.value.badgeId == badgeId }
        return true
    }

    override fun hasUserBadge(
        badgeId: Int,
        userId: Int,
    ): Boolean = userBadges.values.any { it.badgeId == badgeId && it.userId == userId }

    override fun grantBadge(
        badgeId: Int,
        userId: Int,
        grantedByAdminId: Int?,
        reason: String?,
    ): UserBadgeDetailRecord {
        val badge = badges[badgeId] ?: error("Badge $badgeId not found")
        val granted =
            UserBadgeDetailRecord(
                id = userBadgeSequence++,
                userId = userId,
                badgeId = badgeId,
                grantedByAdminId = grantedByAdminId,
                reason = reason,
                grantedAt = Instant.now(),
                badge = badge.copy(holderCount = badge.holderCount + 1),
            )
        userBadges[granted.id] = granted
        badges[badgeId] = badge.copy(holderCount = badge.holderCount + 1, updatedAt = Instant.now())
        return granted.copy(badge = badges[badgeId]!!)
    }

    override fun revokeBadge(
        badgeId: Int,
        userId: Int,
    ): Boolean {
        val target = userBadges.values.firstOrNull { it.badgeId == badgeId && it.userId == userId } ?: return false
        userBadges.remove(target.id)
        badges[badgeId]?.let { badge ->
            badges[badgeId] = badge.copy(holderCount = (badge.holderCount - 1).coerceAtLeast(0), updatedAt = Instant.now())
        }
        return true
    }

    companion object {
        fun seeded(): InMemoryBadgeRepository {
            val now = Instant.parse("2026-02-11T09:00:00Z")
            val badge1 =
                BadgeRecord(
                    id = 1,
                    name = "리뷰 마스터",
                    description = "리뷰 10개 이상 작성",
                    iconUrl = "/badges/review-master.svg",
                    type = BadgeType.AUTO,
                    condition = BadgeCondition("review_count", 10),
                    rarity = BadgeRarity.COMMON,
                    holderCount = 2,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now,
                )
            val badge2 =
                BadgeRecord(
                    id = 2,
                    name = "전문가",
                    description = "관리자가 인정한 분야 전문가",
                    iconUrl = "/badges/expert.svg",
                    type = BadgeType.MANUAL,
                    condition = null,
                    rarity = BadgeRarity.LEGENDARY,
                    holderCount = 1,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now,
                )
            return InMemoryBadgeRepository(
                badges = listOf(badge1, badge2),
                userBadges =
                    listOf(
                        UserBadgeDetailRecord(1, 4, 1, 1, "초기 부여", now, badge1),
                        UserBadgeDetailRecord(2, 5, 1, 1, "초기 부여", now, badge1),
                        UserBadgeDetailRecord(3, 4, 2, 1, "관리자 수동 부여", now, badge2),
                    ),
                userIds = setOf(1, 2, 4, 5, 6),
            )
        }
    }
}
