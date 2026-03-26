package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push

import java.time.Instant

data class PushSubscriptionRecord(
    val id: Int,
    val userId: Int,
    val endpoint: String,
    val p256dhKey: String,
    val authKey: String,
    val expirationTime: Long?,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class PushPreferenceRecord(
    val id: Int,
    val userId: Int,
    val priceAlertEnabled: Boolean,
    val orderStatusEnabled: Boolean,
    val chatMessageEnabled: Boolean,
    val dealEnabled: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class NewPushSubscription(
    val endpoint: String,
    val p256dhKey: String,
    val authKey: String,
    val expirationTime: Long?,
)

data class PushPreferenceUpdate(
    val priceAlertEnabled: Boolean,
    val orderStatusEnabled: Boolean,
    val chatMessageEnabled: Boolean,
    val dealEnabled: Boolean,
)

interface PushRepository {
    fun upsertSubscription(
        userId: Int,
        subscription: NewPushSubscription,
    ): PushSubscriptionRecord

    fun listSubscriptions(userId: Int): List<PushSubscriptionRecord>

    fun findSubscriptionByEndpoint(
        userId: Int,
        endpoint: String,
    ): PushSubscriptionRecord?

    fun disableSubscription(
        userId: Int,
        endpoint: String,
    ): PushSubscriptionRecord?

    fun findPreferenceByUserId(userId: Int): PushPreferenceRecord?

    fun upsertPreference(
        userId: Int,
        update: PushPreferenceUpdate,
    ): PushPreferenceRecord
}
