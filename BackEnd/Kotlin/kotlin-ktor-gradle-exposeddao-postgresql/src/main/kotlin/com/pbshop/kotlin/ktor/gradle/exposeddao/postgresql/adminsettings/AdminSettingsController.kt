package com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.adminsettings

import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.common.respondStub
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.PbRole
import com.pbshop.kotlin.ktor.gradle.exposeddao.postgresql.security.requireAnyRole
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post

class AdminSettingsController(
    private val service: AdminSettingsService,
) {
    fun Route.register() {
        get("/admin/settings/extensions") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.extensions())
        }
        post("/admin/settings/extensions") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.setExtensions(call.receive()))
        }
        get("/admin/settings/upload-limits") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.uploadLimits())
        }
        patch("/admin/settings/upload-limits") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.updateUploadLimits(call.receive()))
        }
        get("/admin/settings/review-policy") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.reviewPolicy())
        }
        patch("/admin/settings/review-policy") {
            call.requireAnyRole(PbRole.ADMIN)
            call.respondStub(service.updateReviewPolicy(call.receive()))
        }
    }
}
