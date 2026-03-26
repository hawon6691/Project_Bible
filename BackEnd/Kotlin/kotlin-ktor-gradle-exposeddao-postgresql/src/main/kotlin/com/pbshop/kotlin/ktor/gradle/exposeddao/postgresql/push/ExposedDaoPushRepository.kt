package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.DatabaseFactory
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PushPreferencesTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.PushSubscriptionsTable
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.db.exposed.UsersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ExposedDaoPushRepository(
    private val databaseFactory: DatabaseFactory,
) : PushRepository {
    override fun upsertSubscription(
        userId: Int,
        subscription: NewPushSubscription,
    ): PushSubscriptionRecord =
        databaseFactory.withTransaction {
            val existing =
                PushSubscriptionsTable
                    .selectAll()
                    .where { PushSubscriptionsTable.endpoint eq subscription.endpoint }
                    .limit(1)
                    .firstOrNull()
            val now = Instant.now()
            if (existing != null) {
                val id = existing[PushSubscriptionsTable.id].value
                PushSubscriptionsTable.update({ PushSubscriptionsTable.id eq id }) {
                    it[user] = EntityID(userId, UsersTable)
                    it[p256dhKey] = subscription.p256dhKey
                    it[authKey] = subscription.authKey
                    it[expirationTime] = subscription.expirationTime
                    it[isActive] = true
                    it[updatedAt] = now
                }
            } else {
                PushSubscriptionsTable.insert {
                    it[user] = EntityID(userId, UsersTable)
                    it[endpoint] = subscription.endpoint
                    it[p256dhKey] = subscription.p256dhKey
                    it[authKey] = subscription.authKey
                    it[expirationTime] = subscription.expirationTime
                    it[isActive] = true
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }
            requireNotNull(findSubscriptionByEndpoint(userId, subscription.endpoint))
        }

    override fun listSubscriptions(userId: Int): List<PushSubscriptionRecord> =
        databaseFactory.withTransaction {
            PushSubscriptionsTable
                .selectAll()
                .where { (PushSubscriptionsTable.user eq userId) and (PushSubscriptionsTable.isActive eq true) }
                .orderBy(PushSubscriptionsTable.updatedAt to SortOrder.DESC, PushSubscriptionsTable.id to SortOrder.DESC)
                .map(::toSubscription)
        }

    override fun findSubscriptionByEndpoint(
        userId: Int,
        endpoint: String,
    ): PushSubscriptionRecord? =
        databaseFactory.withTransaction {
            PushSubscriptionsTable
                .selectAll()
                .where { (PushSubscriptionsTable.user eq userId) and (PushSubscriptionsTable.endpoint eq endpoint) }
                .limit(1)
                .firstOrNull()
                ?.let(::toSubscription)
        }

    override fun disableSubscription(
        userId: Int,
        endpoint: String,
    ): PushSubscriptionRecord? =
        databaseFactory.withTransaction {
            val existing = findSubscriptionByEndpoint(userId, endpoint) ?: return@withTransaction null
            PushSubscriptionsTable.update({ PushSubscriptionsTable.id eq existing.id }) {
                it[isActive] = false
                it[updatedAt] = Instant.now()
            }
            findSubscriptionByEndpoint(userId, endpoint)
        }

    override fun findPreferenceByUserId(userId: Int): PushPreferenceRecord? =
        databaseFactory.withTransaction {
            PushPreferencesTable
                .selectAll()
                .where { PushPreferencesTable.user eq userId }
                .limit(1)
                .firstOrNull()
                ?.let(::toPreference)
        }

    override fun upsertPreference(
        userId: Int,
        update: PushPreferenceUpdate,
    ): PushPreferenceRecord =
        databaseFactory.withTransaction {
            val existing =
                PushPreferencesTable
                    .selectAll()
                    .where { PushPreferencesTable.user eq userId }
                    .limit(1)
                    .firstOrNull()
            val now = Instant.now()
            if (existing != null) {
                val id = existing[PushPreferencesTable.id].value
                PushPreferencesTable.update({ PushPreferencesTable.id eq id }) {
                    it[priceAlertEnabled] = update.priceAlertEnabled
                    it[orderStatusEnabled] = update.orderStatusEnabled
                    it[chatMessageEnabled] = update.chatMessageEnabled
                    it[dealEnabled] = update.dealEnabled
                    it[updatedAt] = now
                }
            } else {
                PushPreferencesTable.insert {
                    it[user] = EntityID(userId, UsersTable)
                    it[priceAlertEnabled] = update.priceAlertEnabled
                    it[orderStatusEnabled] = update.orderStatusEnabled
                    it[chatMessageEnabled] = update.chatMessageEnabled
                    it[dealEnabled] = update.dealEnabled
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }
            requireNotNull(findPreferenceByUserId(userId))
        }

    private fun toSubscription(row: org.jetbrains.exposed.sql.ResultRow): PushSubscriptionRecord =
        PushSubscriptionRecord(
            id = row[PushSubscriptionsTable.id].value,
            userId = row[PushSubscriptionsTable.user].value,
            endpoint = row[PushSubscriptionsTable.endpoint],
            p256dhKey = row[PushSubscriptionsTable.p256dhKey],
            authKey = row[PushSubscriptionsTable.authKey],
            expirationTime = row[PushSubscriptionsTable.expirationTime],
            isActive = row[PushSubscriptionsTable.isActive],
            createdAt = row[PushSubscriptionsTable.createdAt],
            updatedAt = row[PushSubscriptionsTable.updatedAt],
        )

    private fun toPreference(row: org.jetbrains.exposed.sql.ResultRow): PushPreferenceRecord =
        PushPreferenceRecord(
            id = row[PushPreferencesTable.id].value,
            userId = row[PushPreferencesTable.user].value,
            priceAlertEnabled = row[PushPreferencesTable.priceAlertEnabled],
            orderStatusEnabled = row[PushPreferencesTable.orderStatusEnabled],
            chatMessageEnabled = row[PushPreferencesTable.chatMessageEnabled],
            dealEnabled = row[PushPreferencesTable.dealEnabled],
            createdAt = row[PushPreferencesTable.createdAt],
            updatedAt = row[PushPreferencesTable.updatedAt],
        )
}
