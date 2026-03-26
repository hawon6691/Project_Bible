package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push

import kotlinx.serialization.Serializable

@Serializable
data class PushSubscriptionRequest(
    val endpoint: String,
    val p256dhKey: String,
    val authKey: String,
    val expirationTime: Long? = null,
)

@Serializable
data class PushUnsubscribeRequest(
    val endpoint: String,
)

@Serializable
data class PushPreferenceRequest(
    val priceAlertEnabled: Boolean,
    val orderStatusEnabled: Boolean,
    val chatMessageEnabled: Boolean,
    val dealEnabled: Boolean,
)
