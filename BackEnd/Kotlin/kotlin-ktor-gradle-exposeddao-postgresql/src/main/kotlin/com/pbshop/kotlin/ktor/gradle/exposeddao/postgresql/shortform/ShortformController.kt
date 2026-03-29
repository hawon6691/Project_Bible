package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.shortform

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

class ShortformController(
    private val service: ShortformService,
) {
    fun Route.register() {
        post("/shortforms") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.upload(call.currentUserId(), call.readUploadInput()))
        }
        get("/shortforms/ranking/list") {
            call.respondStub(
                service.ranking(
                    period = call.request.queryParameters["period"],
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10,
                ),
            )
        }
        get("/shortforms/user/{userId}") {
            call.respondStub(
                service.userShortforms(
                    userId = call.pathInt("userId"),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        get("/shortforms/{id}/transcode-status") {
            call.respondStub(service.transcodeStatus(call.pathInt("id")))
        }
        post("/shortforms/{id}/transcode/retry") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.retry(call.currentUserId(), call.pbActor().role == PbRole.ADMIN, call.pathInt("id")))
        }
        post("/shortforms/{id}/like") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.toggleLike(call.currentUserId(), call.pathInt("id")))
        }
        post("/shortforms/{id}/comments") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.createComment(call.currentUserId(), call.pathInt("id"), call.receive()))
        }
        get("/shortforms/{id}/comments") {
            call.respondStub(
                service.comments(
                    shortformId = call.pathInt("id"),
                    page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
        get("/shortforms/{id}") {
            call.respondStub(service.detail(call.pathInt("id")))
        }
        delete("/shortforms/{id}") {
            call.requireAnyRole(PbRole.USER)
            call.respondStub(service.delete(call.currentUserId(), call.pbActor().role == PbRole.ADMIN, call.pathInt("id")))
        }
        get("/shortforms") {
            call.respondStub(
                service.feed(
                    cursor = call.request.queryParameters["cursor"]?.toIntOrNull(),
                    limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20,
                ),
            )
        }
    }

    private suspend fun io.ktor.server.application.ApplicationCall.readUploadInput(): ShortformUploadInput {
        val multipart = receiveMultipart()
        var title = ""
        var fileName: String? = null
        var mimeType = "video/mp4"
        val productIds = mutableListOf<Int>()
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    fileName = part.originalFileName ?: part.name ?: "shortform-video.mp4"
                    mimeType = part.contentType?.toString() ?: mimeType
                }
                is PartData.FormItem -> {
                    when (part.name) {
                        "title" -> title = part.value
                        "productIds", "productIds[]" -> part.value.toIntOrNull()?.let(productIds::add)
                    }
                }
                else -> Unit
            }
            part.dispose()
        }
        return ShortformUploadInput(
            title = title,
            originalFilename = fileName.orEmpty(),
            mimeType = mimeType,
            size = 1024,
            productIds = productIds,
        )
    }

    private fun io.ktor.server.application.ApplicationCall.currentUserId(): Int = pbActor().userId ?: 0

    private fun io.ktor.server.application.ApplicationCall.pathInt(name: String): Int = parameters[name]?.toIntOrNull() ?: 0
}
