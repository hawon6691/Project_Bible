package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push

import java.time.Instant

class InMemoryPushRepository(
    seededSubscriptions: List<PushSubscriptionRecord> = emptyList(),
    seededPreferences: List<PushPreferenceRecord> = emptyList(),
) : PushRepository {
    private val subscriptions = linkedMapOf<Int, PushSubscriptionRecord>()
    private val preferences = linkedMapOf<Int, PushPreferenceRecord>()
    private var nextSubscriptionId = 1
    private var nextPreferenceId = 1

    init {
        seededSubscriptions.forEach {
            subscriptions[it.id] = it
            nextSubscriptionId = maxOf(nextSubscriptionId, it.id + 1)
        }
        seededPreferences.forEach {
            preferences[it.userId] = it
            nextPreferenceId = maxOf(nextPreferenceId, it.id + 1)
        }
    }

    override fun upsertSubscription(
        userId: Int,
        subscription: NewPushSubscription,
    ): PushSubscriptionRecord {
        val now = Instant.now()
        val existing = subscriptions.values.firstOrNull { it.endpoint == subscription.endpoint }
        val updated =
            if (existing != null) {
                existing.copy(
                    userId = userId,
                    p256dhKey = subscription.p256dhKey,
                    authKey = subscription.authKey,
                    expirationTime = subscription.expirationTime,
                    isActive = true,
                    updatedAt = now,
                )
            } else {
                PushSubscriptionRecord(
                    id = nextSubscriptionId++,
                    userId = userId,
                    endpoint = subscription.endpoint,
                    p256dhKey = subscription.p256dhKey,
                    authKey = subscription.authKey,
                    expirationTime = subscription.expirationTime,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now,
                )
            }
        subscriptions[updated.id] = updated
        return updated
    }

    override fun listSubscriptions(userId: Int): List<PushSubscriptionRecord> =
        subscriptions.values.filter { it.userId == userId && it.isActive }.sortedByDescending { it.updatedAt }

    override fun findSubscriptionByEndpoint(
        userId: Int,
        endpoint: String,
    ): PushSubscriptionRecord? = subscriptions.values.firstOrNull { it.userId == userId && it.endpoint == endpoint }

    override fun disableSubscription(
        userId: Int,
        endpoint: String,
    ): PushSubscriptionRecord? {
        val existing = findSubscriptionByEndpoint(userId, endpoint) ?: return null
        val updated = existing.copy(isActive = false, updatedAt = Instant.now())
        subscriptions[updated.id] = updated
        return updated
    }

    override fun findPreferenceByUserId(userId: Int): PushPreferenceRecord? = preferences[userId]

    override fun upsertPreference(
        userId: Int,
        update: PushPreferenceUpdate,
    ): PushPreferenceRecord {
        val now = Instant.now()
        val current = preferences[userId]
        val updated =
            if (current != null) {
                current.copy(
                    priceAlertEnabled = update.priceAlertEnabled,
                    orderStatusEnabled = update.orderStatusEnabled,
                    chatMessageEnabled = update.chatMessageEnabled,
                    dealEnabled = update.dealEnabled,
                    updatedAt = now,
                )
            } else {
                PushPreferenceRecord(
                    id = nextPreferenceId++,
                    userId = userId,
                    priceAlertEnabled = update.priceAlertEnabled,
                    orderStatusEnabled = update.orderStatusEnabled,
                    chatMessageEnabled = update.chatMessageEnabled,
                    dealEnabled = update.dealEnabled,
                    createdAt = now,
                    updatedAt = now,
                )
            }
        preferences[userId] = updated
        return updated
    }

    companion object {
        fun seeded(): InMemoryPushRepository {
            val now = Instant.now()
            return InMemoryPushRepository(
                seededSubscriptions =
                    listOf(
                        PushSubscriptionRecord(
                            id = 1,
                            userId = 4,
                            endpoint = "https://push.example.com/sub/abc123",
                            p256dhKey = "p256dh_key_value",
                            authKey = "auth_key_value",
                            expirationTime = null,
                            isActive = true,
                            createdAt = now.minusSeconds(86400),
                            updatedAt = now.minusSeconds(86400),
                        ),
                    ),
                seededPreferences =
                    listOf(
                        PushPreferenceRecord(1, 4, true, true, true, true, now.minusSeconds(86400), now.minusSeconds(86400)),
                        PushPreferenceRecord(2, 5, true, false, true, false, now.minusSeconds(86400), now.minusSeconds(86400)),
                    ),
            )
        }
    }
}
