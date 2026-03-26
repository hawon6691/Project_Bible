package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.push

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun pushOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Post, "/push/subscriptions", "Push", "Create push subscription", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "endpoint" to "https://fcm.googleapis.com/fcm/send/..."))
        },
        endpoint(HttpMethod.Post, "/push/subscriptions/unsubscribe", "Push", "Unsubscribe push", roles = setOf(PbRole.USER)) { message("Push subscription disabled.", "success" to true) },
        endpoint(HttpMethod.Get, "/push/subscriptions", "Push", "Push subscription list", roles = setOf(PbRole.USER)) { StubResponse(data = listOf(mapOf("id" to 1, "isActive" to true))) },
        endpoint(HttpMethod.Get, "/push/preferences", "Push", "Push preferences", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("priceAlertEnabled" to true, "orderStatusEnabled" to true, "chatMessageEnabled" to true, "dealEnabled" to true)) },
        endpoint(HttpMethod.Post, "/push/preferences", "Push", "Update push preferences", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("priceAlertEnabled" to false, "orderStatusEnabled" to true, "chatMessageEnabled" to false, "dealEnabled" to true)) },
    )
