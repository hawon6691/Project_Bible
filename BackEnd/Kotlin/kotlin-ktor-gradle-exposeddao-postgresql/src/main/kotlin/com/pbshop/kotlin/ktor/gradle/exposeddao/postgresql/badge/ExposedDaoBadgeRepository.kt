package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.badge

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.BadgesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UserBadgesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ExposedDaoBadgeRepository(
    private val databaseFactory: DatabaseFactory,
) : BadgeRepository {
    override fun listBadges(): List<BadgeRecord> =
        databaseFactory.withTransaction {
            BadgesTable.selectAll()
                .where { BadgesTable.isActive eq true }
                .orderBy(BadgesTable.id to SortOrder.ASC)
                .map(::toBadgeRecord)
        }

    override fun findBadgeById(id: Int): BadgeRecord? =
        databaseFactory.withTransaction {
            BadgesTable.selectAll()
                .where { (BadgesTable.id eq id) and (BadgesTable.isActive eq true) }
                .singleOrNull()
                ?.let(::toBadgeRecord)
        }

    override fun findBadgeByName(name: String): BadgeRecord? =
        databaseFactory.withTransaction {
            BadgesTable.selectAll()
                .where { (BadgesTable.name eq name) and (BadgesTable.isActive eq true) }
                .singleOrNull()
                ?.let(::toBadgeRecord)
        }

    override fun userExists(userId: Int): Boolean =
        databaseFactory.withTransaction {
            !UsersTable.selectAll().where { UsersTable.id eq userId }.empty()
        }

    override fun listUserBadges(userId: Int): List<UserBadgeDetailRecord> =
        databaseFactory.withTransaction {
            UserBadgesTable.innerJoin(BadgesTable)
                .selectAll()
                .where { UserBadgesTable.user eq userId }
                .orderBy(UserBadgesTable.grantedAt to SortOrder.DESC)
                .map { row ->
                    UserBadgeDetailRecord(
                        id = row[UserBadgesTable.id].value,
                        userId = row[UserBadgesTable.user].value,
                        badgeId = row[UserBadgesTable.badge].value,
                        grantedByAdminId = row[UserBadgesTable.grantedByAdminId]?.value,
                        reason = row[UserBadgesTable.reason],
                        grantedAt = row[UserBadgesTable.grantedAt],
                        badge = toBadgeRecord(row),
                    )
                }
        }

    override fun createBadge(newBadge: NewBadge): BadgeRecord =
        databaseFactory.withTransaction {
            val now = Instant.now()
            val insertedId =
                BadgesTable.insert {
                    it[name] = newBadge.name
                    it[description] = newBadge.description
                    it[iconUrl] = newBadge.iconUrl
                    it[type] = newBadge.type.name
                    it[conditionJson] = newBadge.condition?.let(::encodeCondition)
                    it[rarity] = newBadge.rarity.name
                    it[holderCount] = 0
                    it[isActive] = true
                    it[createdAt] = now
                    it[updatedAt] = now
                } get BadgesTable.id
            BadgeRecord(insertedId.value, newBadge.name, newBadge.description, newBadge.iconUrl, newBadge.type, newBadge.condition, newBadge.rarity, 0, true, now, now)
        }

    override fun updateBadge(
        badgeId: Int,
        update: BadgeUpdate,
    ): BadgeRecord =
        databaseFactory.withTransaction {
            val current = findBadgeById(badgeId) ?: error("Badge $badgeId not found")
            val now = Instant.now()
            BadgesTable.update({ BadgesTable.id eq badgeId }) {
                it[name] = update.name ?: current.name
                it[description] = update.description ?: current.description
                it[iconUrl] = update.iconUrl ?: current.iconUrl
                it[type] = (update.type ?: current.type).name
                it[conditionJson] = (update.condition ?: current.condition)?.let(::encodeCondition)
                it[rarity] = (update.rarity ?: current.rarity).name
                it[updatedAt] = now
            }
            current.copy(
                name = update.name ?: current.name,
                description = update.description ?: current.description,
                iconUrl = update.iconUrl ?: current.iconUrl,
                type = update.type ?: current.type,
                condition = update.condition ?: current.condition,
                rarity = update.rarity ?: current.rarity,
                updatedAt = now,
            )
        }

    override fun deleteBadge(badgeId: Int): Boolean =
        databaseFactory.withTransaction {
            if (findBadgeById(badgeId) == null) {
                return@withTransaction false
            }
            UserBadgesTable.deleteWhere { UserBadgesTable.badge eq badgeId }
            BadgesTable.update({ BadgesTable.id eq badgeId }) {
                it[isActive] = false
                it[holderCount] = 0
                it[updatedAt] = Instant.now()
            }
            true
        }

    override fun hasUserBadge(
        badgeId: Int,
        userId: Int,
    ): Boolean =
        databaseFactory.withTransaction {
            !UserBadgesTable.selectAll()
                .where { (UserBadgesTable.badge eq badgeId) and (UserBadgesTable.user eq userId) }
                .empty()
        }

    override fun grantBadge(
        badgeId: Int,
        userId: Int,
        grantedByAdminId: Int?,
        reason: String?,
    ): UserBadgeDetailRecord =
        databaseFactory.withTransaction {
            val badge = findBadgeById(badgeId) ?: error("Badge $badgeId not found")
            val now = Instant.now()
            val insertedId =
                UserBadgesTable.insert {
                    it[UserBadgesTable.user] = org.jetbrains.exposed.dao.id.EntityID(userId, UsersTable)
                    it[UserBadgesTable.badge] = org.jetbrains.exposed.dao.id.EntityID(badgeId, BadgesTable)
                    it[UserBadgesTable.grantedByAdminId] = grantedByAdminId?.let { adminId -> org.jetbrains.exposed.dao.id.EntityID(adminId, UsersTable) }
                    it[UserBadgesTable.reason] = reason
                    it[UserBadgesTable.grantedAt] = now
                    it[UserBadgesTable.createdAt] = now
                    it[UserBadgesTable.updatedAt] = now
                } get UserBadgesTable.id
            BadgesTable.update({ BadgesTable.id eq badgeId }) {
                it[holderCount] = badge.holderCount + 1
                it[updatedAt] = now
            }
            UserBadgeDetailRecord(
                id = insertedId.value,
                userId = userId,
                badgeId = badgeId,
                grantedByAdminId = grantedByAdminId,
                reason = reason,
                grantedAt = now,
                badge = badge.copy(holderCount = badge.holderCount + 1, updatedAt = now),
            )
        }

    override fun revokeBadge(
        badgeId: Int,
        userId: Int,
    ): Boolean =
        databaseFactory.withTransaction {
            val target =
                UserBadgesTable.selectAll()
                    .where { (UserBadgesTable.badge eq badgeId) and (UserBadgesTable.user eq userId) }
                    .singleOrNull()
                    ?: return@withTransaction false
            UserBadgesTable.deleteWhere { UserBadgesTable.id eq target[UserBadgesTable.id].value }
            BadgesTable.selectAll().where { BadgesTable.id eq badgeId }.singleOrNull()?.let { badge ->
                BadgesTable.update({ BadgesTable.id eq badgeId }) {
                    it[holderCount] = (badge[BadgesTable.holderCount] - 1).coerceAtLeast(0)
                    it[updatedAt] = Instant.now()
                }
            }
            true
        }

    private fun toBadgeRecord(row: org.jetbrains.exposed.sql.ResultRow): BadgeRecord =
        BadgeRecord(
            id = row[BadgesTable.id].value,
            name = row[BadgesTable.name],
            description = row[BadgesTable.description],
            iconUrl = row[BadgesTable.iconUrl],
            type = BadgeType.valueOf(row[BadgesTable.type]),
            condition = row[BadgesTable.conditionJson]?.let(::decodeCondition),
            rarity = BadgeRarity.valueOf(row[BadgesTable.rarity]),
            holderCount = row[BadgesTable.holderCount],
            isActive = row[BadgesTable.isActive],
            createdAt = row[BadgesTable.createdAt],
            updatedAt = row[BadgesTable.updatedAt],
        )

    private fun encodeCondition(condition: BadgeCondition): String =
        """{"metric":"${condition.metric}","threshold":${condition.threshold}}"""

    private fun decodeCondition(raw: String): BadgeCondition {
        val json = Json.parseToJsonElement(raw).jsonObject
        return BadgeCondition(
            metric = json["metric"]!!.jsonPrimitive.content,
            threshold = json["threshold"]!!.jsonPrimitive.content.toInt(),
        )
    }
}
