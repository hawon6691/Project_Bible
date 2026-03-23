package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun mediaOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Post, "/images/upload", "Image", "Upload image", roles = setOf(PbRole.USER, PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "originalUrl" to "/uploads/original/abc123.jpg", "variants" to listOf(mapOf("type" to "THUMBNAIL", "url" to "/uploads/thumb/abc123.webp")), "processingStatus" to "COMPLETED"))
        },
        endpoint(HttpMethod.Post, "/upload/image", "Image", "Legacy image upload", roles = setOf(PbRole.USER, PbRole.ADMIN)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("url" to "/uploads/original/legacy-image.jpg"))
        },
        endpoint(HttpMethod.Get, "/images/{id}/variants", "Image", "Image variants") { call ->
            StubResponse(data = listOf(mapOf("imageId" to call.pathParam("id", "1"), "type" to "THUMBNAIL", "url" to "/uploads/thumb/abc123.webp")))
        },
        endpoint(HttpMethod.Delete, "/images/{id}", "Image", "Delete image", roles = setOf(PbRole.ADMIN)) { message("Image deleted.") },
        endpoint(HttpMethod.Post, "/media/upload", "Media", "Upload media", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = listOf(mapOf("id" to 1, "mime" to "video/mp4", "ownerType" to "product")))
        },
        endpoint(HttpMethod.Post, "/media/presigned-url", "Media", "Create presigned URL", roles = setOf(PbRole.USER)) {
            StubResponse(data = mapOf("uploadUrl" to "https://storage.pbshop.dev/upload/1", "fileKey" to "media/1.mp4"))
        },
        endpoint(HttpMethod.Get, "/media/stream/{id}", "Media", "Stream media") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "streamUrl" to "/media/stream/${call.pathParam("id", "1")}"))
        },
        endpoint(HttpMethod.Delete, "/media/{id}", "Media", "Delete media", roles = setOf(PbRole.USER)) { message("Media deleted.") },
        endpoint(HttpMethod.Get, "/media/{id}/metadata", "Media", "Media metadata") { call ->
            StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "mime" to "video/mp4", "size" to 1024000))
        },
        endpoint(HttpMethod.Get, "/badges", "Badge", "Badge list") { StubResponse(data = listOf(mapOf("id" to 1, "name" to "Review Master", "holderCount" to 1523))) },
        endpoint(HttpMethod.Get, "/badges/me", "Badge", "Current user badges", roles = setOf(PbRole.USER)) { StubResponse(data = listOf(mapOf("id" to 1, "name" to "Review Master"))) },
        endpoint(HttpMethod.Get, "/users/{id}/badges", "Badge", "User badges") { call -> StubResponse(data = listOf(mapOf("userId" to call.pathParam("id", "1"), "badge" to "Review Master"))) },
        endpoint(HttpMethod.Post, "/admin/badges", "Badge", "Create badge", roles = setOf(PbRole.ADMIN)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 2, "name" to "Purchase King")) },
        endpoint(HttpMethod.Patch, "/admin/badges/{id}", "Badge", "Update badge", roles = setOf(PbRole.ADMIN)) { call -> StubResponse(data = mapOf("id" to call.pathParam("id", "1"), "name" to "Updated badge")) },
        endpoint(HttpMethod.Delete, "/admin/badges/{id}", "Badge", "Delete badge", roles = setOf(PbRole.ADMIN)) { message("Badge deleted.") },
        endpoint(HttpMethod.Post, "/admin/badges/{id}/grant", "Badge", "Grant badge", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(status = HttpStatusCode.Created, data = mapOf("badgeId" to call.pathParam("id", "1"), "userId" to 1))
        },
        endpoint(HttpMethod.Delete, "/admin/badges/{id}/revoke/{userId}", "Badge", "Revoke badge", roles = setOf(PbRole.ADMIN)) { message("Badge revoked.") },
        endpoint(HttpMethod.Post, "/push/subscriptions", "Push", "Create push subscription", roles = setOf(PbRole.USER)) {
            StubResponse(status = HttpStatusCode.Created, data = mapOf("id" to 1, "endpoint" to "https://fcm.googleapis.com/fcm/send/..."))
        },
        endpoint(HttpMethod.Post, "/push/subscriptions/unsubscribe", "Push", "Unsubscribe push", roles = setOf(PbRole.USER)) { message("Push subscription disabled.", "success" to true) },
        endpoint(HttpMethod.Get, "/push/subscriptions", "Push", "Push subscription list", roles = setOf(PbRole.USER)) { StubResponse(data = listOf(mapOf("id" to 1, "isActive" to true))) },
        endpoint(HttpMethod.Get, "/push/preferences", "Push", "Push preferences", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("marketing" to true, "priceAlerts" to true)) },
        endpoint(HttpMethod.Post, "/push/preferences", "Push", "Update push preferences", roles = setOf(PbRole.USER)) { StubResponse(data = mapOf("marketing" to false, "priceAlerts" to true)) },
        endpoint(HttpMethod.Post, "/admin/push/send", "Push", "Send admin push", roles = setOf(PbRole.ADMIN)) { StubResponse(data = mapOf("sentCount" to 1523, "scheduledAt" to null)) },
    )
