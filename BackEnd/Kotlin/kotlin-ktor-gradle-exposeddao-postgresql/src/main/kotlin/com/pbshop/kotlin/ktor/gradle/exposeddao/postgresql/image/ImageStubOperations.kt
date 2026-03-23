package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.image

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubOperation
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.StubResponse
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.endpoint
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.message
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

fun imageOperations(): List<StubOperation> =
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
        endpoint(HttpMethod.Post, "/products/{id}/images", "Image", "Attach product image", roles = setOf(PbRole.ADMIN)) { call ->
            StubResponse(status = HttpStatusCode.Created, data = mapOf("productId" to call.pathParam("id", "1"), "imageId" to 1))
        },
        endpoint(HttpMethod.Delete, "/products/{id}/images/{imageId}", "Image", "Delete product image", roles = setOf(PbRole.ADMIN)) { message("Product image deleted.") },
        endpoint(HttpMethod.Post, "/users/me/profile-image", "Image", "Upload profile image", roles = setOf(PbRole.USER)) { StubResponse(status = HttpStatusCode.Created, data = mapOf("profileImageUrl" to "/uploads/profile/user-1.webp")) },
        endpoint(HttpMethod.Delete, "/users/me/profile-image", "Image", "Delete profile image", roles = setOf(PbRole.USER)) { message("Profile image deleted.") },
    )
