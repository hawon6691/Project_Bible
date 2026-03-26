package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.PbShopException
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import io.ktor.http.HttpStatusCode
import java.time.Instant

class PushService(
    private val repository: PushRepository,
) {
    fun subscribe(
        userId: Int,
        request: PushSubscriptionRequest,
    ): StubResponse {
        val endpoint = request.endpoint.trim()
        val p256dhKey = request.p256dhKey.trim()
        val authKey = request.authKey.trim()
        if (endpoint.isBlank() || p256dhKey.isBlank() || authKey.isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "endpoint, p256dhKey, authKey는 필수입니다.")
        }
        val created =
            repository.upsertSubscription(
                userId,
                NewPushSubscription(
                    endpoint = endpoint,
                    p256dhKey = p256dhKey,
                    authKey = authKey,
                    expirationTime = request.expirationTime,
                ),
            )
        return StubResponse(status = HttpStatusCode.Created, data = subscriptionPayload(created))
    }

    fun subscriptions(userId: Int): StubResponse =
        StubResponse(data = repository.listSubscriptions(userId).map(::subscriptionPayload))

    fun unsubscribe(
        userId: Int,
        request: PushUnsubscribeRequest,
    ): StubResponse {
        val endpoint = request.endpoint.trim()
        if (endpoint.isBlank()) {
            throw PbShopException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "endpoint는 필수입니다.")
        }
        val disabled =
            repository.disableSubscription(userId, endpoint)
                ?: throw PbShopException(HttpStatusCode.NotFound, "PUSH_SUBSCRIPTION_NOT_FOUND", "푸시 구독을 찾을 수 없습니다.")
        return StubResponse(
            data =
                mapOf(
                    "message" to "Push subscription disabled.",
                    "success" to true,
                    "subscription" to subscriptionPayload(disabled),
                ),
        )
    }

    fun preferences(userId: Int): StubResponse =
        StubResponse(data = preferencePayload(repository.findPreferenceByUserId(userId) ?: defaultPreference(userId)))

    fun updatePreferences(
        userId: Int,
        request: PushPreferenceRequest,
    ): StubResponse =
        StubResponse(
            data =
                preferencePayload(
                    repository.upsertPreference(
                        userId,
                        PushPreferenceUpdate(
                            priceAlertEnabled = request.priceAlertEnabled,
                            orderStatusEnabled = request.orderStatusEnabled,
                            chatMessageEnabled = request.chatMessageEnabled,
                            dealEnabled = request.dealEnabled,
                        ),
                    ),
                ),
        )

    private fun subscriptionPayload(record: PushSubscriptionRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "endpoint" to record.endpoint,
            "p256dhKey" to record.p256dhKey,
            "authKey" to record.authKey,
            "expirationTime" to record.expirationTime,
            "isActive" to record.isActive,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun preferencePayload(record: PushPreferenceRecord): Map<String, Any?> =
        mapOf(
            "id" to record.id,
            "priceAlertEnabled" to record.priceAlertEnabled,
            "orderStatusEnabled" to record.orderStatusEnabled,
            "chatMessageEnabled" to record.chatMessageEnabled,
            "dealEnabled" to record.dealEnabled,
            "createdAt" to record.createdAt.toString(),
            "updatedAt" to record.updatedAt.toString(),
        )

    private fun defaultPreference(userId: Int): PushPreferenceRecord {
        val now = Instant.now()
        return PushPreferenceRecord(
            id = 0,
            userId = userId,
            priceAlertEnabled = true,
            orderStatusEnabled = true,
            chatMessageEnabled = true,
            dealEnabled = true,
            createdAt = now,
            updatedAt = now,
        )
    }
}
