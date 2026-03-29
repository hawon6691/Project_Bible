package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.media

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.pbActor
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post

class MediaController(
    private val service: MediaService,
) {
    fun Route.register() {
        post("/media/upload") {
            call.requireAnyRole(PbRole.USER, PbRole.ADMIN)
            val payload = call.readUploadPayload()
            call.respondStub(service.upload(call.currentUserId(), payload.ownerType, payload.ownerId, payload.files))
        }
        post("/media/presigned-url") {
            call.requireAnyRole(PbRole.USER, PbRole.ADMIN)
            call.respondStub(service.createPresignedUrl(call.currentUserId(), call.receive()))
        }
        get("/media/{id}/metadata") {
            call.respondStub(service.metadata(call.pathInt("id")))
        }
        get("/media/stream/{id}") {
            call.respondStub(service.stream(call.pathInt("id")))
        }
        delete("/media/{id}") {
            call.requireAnyRole(PbRole.USER, PbRole.ADMIN)
            call.respondStub(service.delete(call.currentUserId(), call.pbActor().role == PbRole.ADMIN, call.pathInt("id")))
        }
    }

    private suspend fun io.ktor.server.application.ApplicationCall.readUploadPayload(): MediaUploadPayload {
        val multipart = receiveMultipart()
        val files = mutableListOf<MediaUploadItem>()
        var ownerType = ""
        var ownerId: Int? = null
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    files +=
                        MediaUploadItem(
                            originalName = part.originalFileName ?: part.name ?: "asset.bin",
                            mime = part.contentType?.toString() ?: "application/octet-stream",
                            size = 1024L,
                        )
                }
                is PartData.FormItem -> {
                    when (part.name) {
                        "ownerType" -> ownerType = part.value
                        "ownerId" -> ownerId = part.value.toIntOrNull()
                    }
                }
                else -> Unit
            }
            part.dispose()
        }
        return MediaUploadPayload(ownerType = ownerType, ownerId = ownerId, files = files)
    }

    private fun io.ktor.server.application.ApplicationCall.pathInt(name: String): Int = parameters[name]?.toIntOrNull() ?: 0

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int = pbActor().userId ?: 0
}

private data class MediaUploadPayload(
    val ownerType: String,
    val ownerId: Int?,
    val files: List<MediaUploadItem>,
)
