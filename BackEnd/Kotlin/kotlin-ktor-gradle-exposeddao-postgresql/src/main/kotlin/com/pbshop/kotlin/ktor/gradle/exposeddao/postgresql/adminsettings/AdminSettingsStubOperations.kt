package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.adminsettings

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod

fun adminSettingsOperations(): List<StubOperation> =
    listOf(
        endpoint(HttpMethod.Get, "/admin/settings/extensions", "Admin Settings", "Upload extension settings", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("extensions" to listOf("jpg", "png", "webp", "mp4")))
        },
        endpoint(HttpMethod.Post, "/admin/settings/extensions", "Admin Settings", "Update upload extension settings", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("extensions" to listOf("jpg", "png", "webp", "mp4", "mp3")))
        },
        endpoint(HttpMethod.Get, "/admin/settings/upload-limits", "Admin Settings", "Upload limits", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("image" to 5, "video" to 100, "audio" to 20))
        },
        endpoint(HttpMethod.Patch, "/admin/settings/upload-limits", "Admin Settings", "Update upload limits", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("image" to 8, "video" to 120, "audio" to 20))
        },
        endpoint(HttpMethod.Get, "/admin/settings/review-policy", "Admin Settings", "Review policy", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("maxImageCount" to 10, "pointAmount" to 500))
        },
        endpoint(HttpMethod.Patch, "/admin/settings/review-policy", "Admin Settings", "Update review policy", roles = setOf(PbRole.ADMIN)) {
            StubResponse(data = mapOf("maxImageCount" to 12, "pointAmount" to 700))
        },
    )
