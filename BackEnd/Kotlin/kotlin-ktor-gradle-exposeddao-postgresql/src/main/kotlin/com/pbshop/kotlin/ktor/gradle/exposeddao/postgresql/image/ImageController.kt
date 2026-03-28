package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.image

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.pbActor
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post

class ImageController(
    private val service: ImageService,
) {
    fun Route.register() {
        post("/images/upload") {
            call.requireAnyRole(PbRole.USER, PbRole.ADMIN)
            val upload = call.readUploadInput(requireCategory = true, fallbackCategory = null)
            call.respondStub(service.upload(upload))
        }
        post("/upload/image") {
            call.requireAnyRole(PbRole.USER, PbRole.ADMIN)
            val upload = call.readUploadInput(requireCategory = false, fallbackCategory = "community")
            call.respondStub(service.uploadLegacy(upload))
        }
        get("/images/{id}/variants") {
            call.respondStub(service.variants(call.parameters["id"]?.toIntOrNull() ?: 0))
        }
        delete("/images/{id}") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.delete(call.parameters["id"]?.toIntOrNull() ?: 0))
        }
    }

    private suspend fun io.ktor.server.application.ApplicationCall.readUploadInput(
        requireCategory: Boolean,
        fallbackCategory: String?,
    ): ImageUploadInput {
        val actor = pbActor()
        val multipart = receiveMultipart()
        var fileName: String? = null
        var mimeType = "application/octet-stream"
        var size = 0
        var category: String? = fallbackCategory

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    fileName = part.originalFileName ?: part.name ?: "image.bin"
                    mimeType = part.contentType?.toString() ?: mimeType
                    size = 1024
                }
                is PartData.FormItem -> {
                    if (part.name == "category") {
                        category = part.value
                    }
                }
                else -> Unit
            }
            part.dispose()
        }

        return ImageUploadInput(
            uploadedByUserId = actor.userId,
            originalFilename = fileName.orEmpty(),
            mimeType = mimeType,
            size = size,
            category =
                category
                    ?: if (requireCategory) {
                        ""
                    } else {
                        fallbackCategory.orEmpty()
                    },
        )
    }
}
