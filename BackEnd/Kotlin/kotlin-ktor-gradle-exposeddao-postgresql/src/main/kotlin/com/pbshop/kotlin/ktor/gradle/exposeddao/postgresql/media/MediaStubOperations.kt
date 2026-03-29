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
        endpoint(HttpMethod.Post, "/media/upload", "Media", "Upload media", roles = setOf(PbRole.USER)) {
            StubResponse(
                status = HttpStatusCode.Created,
                data = listOf(
                    mapOf(
                        "id" to 1,
                        "type" to "VIDEO",
                        "mime" to "video/mp4",
                        "ownerType" to "PRODUCT",
                        "ownerId" to 1,
                        "fileUrl" to "https://cdn.pbshop.dev/media/1/original.mp4",
                    )
                ),
            )
        },
        endpoint(HttpMethod.Post, "/media/presigned-url", "Media", "Create presigned URL", roles = setOf(PbRole.USER)) {
            StubResponse(
                status = HttpStatusCode.Created,
                data = mapOf(
                    "uploadUrl" to "https://storage.pbshop.dev/upload/1",
                    "fileKey" to "media/1.mp4",
                    "expiresInSec" to 900,
                ),
            )
        },
        endpoint(HttpMethod.Get, "/media/stream/{id}", "Media", "Stream media") { call ->
            StubResponse(
                status = HttpStatusCode.PartialContent,
                data = mapOf(
                    "id" to call.pathParam("id", "1"),
                    "fileUrl" to "https://cdn.pbshop.dev/media/${call.pathParam("id", "1")}/stream.mp4",
                    "mime" to "video/mp4",
                    "size" to 1024000,
                ),
                meta = mapOf("streamable" to true),
            )
        },
        endpoint(HttpMethod.Delete, "/media/{id}", "Media", "Delete media", roles = setOf(PbRole.USER)) { message("Media deleted.") },
        endpoint(HttpMethod.Get, "/media/{id}/metadata", "Media", "Media metadata") { call ->
            StubResponse(
                data = mapOf(
                    "id" to call.pathParam("id", "1"),
                    "mime" to "video/mp4",
                    "size" to 1024000,
                    "duration" to 42,
                    "resolution" to "1920x1080",
                )
            )
        },
    )
